package com.ilham.notifshot.application.notification;

import com.ilham.notifshot.config.KafkaConfig;
import com.ilham.notifshot.domain.campaign.Campaign;
import com.ilham.notifshot.domain.campaign.CampaignStatus;
import com.ilham.notifshot.domain.notification.*;
import com.ilham.notifshot.domain.recipient.Recipient;
import com.ilham.notifshot.domain.tenant.Tenant;
import com.ilham.notifshot.infrastructure.provider.ProviderResponse;
import com.ilham.notifshot.infrastructure.provider.SimulatedNotificationProvider;
import com.ilham.notifshot.infrastructure.ratelimit.ChannelRateLimiter;
import com.ilham.notifshot.repository.*;
import com.ilham.notifshot.rules.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationJobConsumer {

    private final NotificationJobRepository notificationJobRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final RecipientRepository recipientRepository;
    private final TenantRepository tenantRepository;
    private final CampaignRepository campaignRepository;
    private final SimulatedNotificationProvider provider;
    private final ChannelRateLimiter rateLimiter;
    private final RuleEngine ruleEngine;
    private final NotificationProducer notificationProducer;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_NOTIFICATION_JOBS,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    @Transactional
    public void consume(NotificationJobMessage message) {
        String jobId = message.getNotificationJobId().toString();
        String tenantId = message.getTenantId().toString();
        String campaignId = message.getCampaignId().toString();

        log.info("Processing job={} tenant={} campaign={} retry={}",
                jobId, tenantId, campaignId, message.getRetryCount());

        NotificationJob job = notificationJobRepository
                .findById(message.getNotificationJobId())
                .orElseGet(() -> {
                    log.warn("Job {} not found — skipping", jobId);
                    return null;
                });

        if (job == null) return;

        log.info("job={} campaign.transactional={} channel={}", jobId, job.getCampaign().isTransactional(), job.getChannel());

// Idempotency check — already sent
        if (job.getStatus() == NotificationStatus.SENT) {
            log.info("Job {} already SENT — skipping duplicate", jobId);
            return;
        }

// If not PENDING or PROCESSING, skip
        if (job.getStatus() != NotificationStatus.PENDING &&
                job.getStatus() != NotificationStatus.PROCESSING) {
            log.info("Job {} has status {} — skipping", jobId, job.getStatus());
            return;
        }

        // Mark as processing
        job.setStatus(NotificationStatus.PROCESSING);
        notificationJobRepository.save(job);

        Recipient recipient = job.getRecipient();
        Tenant tenant = job.getTenant();

        // Evaluate rules
        RuleContext context = RuleContext.builder()
                .tenant(tenant)
                .campaign(job.getCampaign())
                .recipient(recipient)
                .channel(job.getChannel())
                .recipientCount(1)
                .retryCount(job.getRetryCount())
                .build();

        RuleResult ruleResult = ruleEngine.evaluate(context);

        switch (ruleResult.getAction()) {
            case SKIP -> {
                job.setStatus(NotificationStatus.SKIPPED);
                notificationJobRepository.save(job);
                campaignRepository.incrementSkippedCount(job.getCampaign().getId());
                log.info("job={} SKIPPED reason={}", jobId, ruleResult.getReason());
                checkAndCompleteCampaign(job);
                return;
            }
            case DELAY -> {
                job.setStatus(NotificationStatus.DELAYED);
                job.setNextRetryAt(LocalDateTime.now().plusHours(1));
                notificationJobRepository.save(job);
                log.info("job={} DELAYED reason={}", jobId, ruleResult.getReason());
                return;
            }
            case REJECT, DISCARD -> {
                job.setStatus(NotificationStatus.FAILED);
                notificationJobRepository.save(job);
                campaignRepository.incrementFailedCount(job.getCampaign().getId());
                log.info("job={} {} reason={}", jobId, ruleResult.getAction(), ruleResult.getReason());
                checkAndCompleteCampaign(job);
                return;
            }
            default -> { /* ALLOW — continue */ }
        }

        // Rate limiting
        try {
            rateLimiter.consumeBlocking(job.getChannel());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // Determine recipient address
        String address = job.getChannel() == Channel.EMAIL
                ? recipient.getEmail()
                : recipient.getPhone();

        // Send via provider
        ProviderResponse response = provider.send(
                job.getChannel(),
                address,
                job.getCampaign().getMessageTemplate(),
                tenantId
        );

        // Record delivery attempt
        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .notificationJob(job)
                .tenantId(job.getTenant().getId())
                .attemptNumber(job.getRetryCount() + 1)
                .status(response.isSuccess() ? "SENT" : "FAILED")
                .providerResponse(response.getMessage())
                .build();
        deliveryAttemptRepository.save(attempt);

        if (response.isSuccess()) {
            job.setStatus(NotificationStatus.SENT);
            notificationJobRepository.save(job);
            campaignRepository.incrementSentCount(job.getCampaign().getId());
            log.info("job={} tenant={} campaign={} SENT", jobId, tenantId, campaignId);
            checkAndCompleteCampaign(job);
        } else {
            handleFailure(job, message);
        }
    }

    private void handleFailure(NotificationJob job, NotificationJobMessage message) {
        job.incrementRetry();

        if (job.canRetry()) {
            job.setStatus(NotificationStatus.PENDING);
            notificationJobRepository.save(job);

            NotificationJobMessage retryMessage = NotificationJobMessage.builder()
                    .notificationJobId(job.getId())
                    .tenantId(job.getTenant().getId())
                    .campaignId(job.getCampaign().getId())
                    .recipientId(job.getRecipient().getId())
                    .channel(job.getChannel())
                    .idempotencyKey(job.getIdempotencyKey())
                    .retryCount(job.getRetryCount())
                    .build();

            notificationProducer.sendRetryJob(retryMessage);
            log.info("job={} scheduled for retry attempt={}", job.getId(), job.getRetryCount());
        } else {
            job.setStatus(NotificationStatus.FAILED);
            notificationJobRepository.save(job);
            campaignRepository.incrementFailedCount(job.getCampaign().getId());
            log.warn("job={} FAILED permanently after {} attempts", job.getId(), job.getRetryCount());
            checkAndCompleteCampaign(job);
        }
    }

    private void checkAndCompleteCampaign(NotificationJob job) {
        UUID campaignId = job.getCampaign().getId();

        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) return;

        // Don't overwrite already completed campaigns
        if (campaign.getStatus() == CampaignStatus.COMPLETED ||
                campaign.getStatus() == CampaignStatus.FAILED) return;

        long pending = notificationJobRepository
                .countByCampaignIdAndStatus(campaignId, NotificationStatus.PENDING);
        long processing = notificationJobRepository
                .countByCampaignIdAndStatus(campaignId, NotificationStatus.PROCESSING);

        if (pending > 0 || processing > 0) return;

        long totalJobs = notificationJobRepository.countByCampaignId(campaignId);
        long failedJobs = notificationJobRepository
                .countByCampaignIdAndStatus(campaignId, NotificationStatus.FAILED);

        campaign.setStatus(failedJobs == totalJobs
                ? CampaignStatus.FAILED
                : CampaignStatus.COMPLETED);

        campaignRepository.save(campaign);
        log.info("Campaign {} marked as {}", campaignId, campaign.getStatus());
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_NOTIFICATION_RETRY,
            groupId = "${spring.kafka.consumer.group-id}-retry"
    )
    @Transactional
    public void consumeRetry(NotificationJobMessage message) {
        log.info("Processing retry job={} attempt={}",
                message.getNotificationJobId(), message.getRetryCount());
        consume(message);
    }
}