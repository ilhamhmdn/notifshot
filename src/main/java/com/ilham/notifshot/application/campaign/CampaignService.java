package com.ilham.notifshot.application.campaign;

import com.ilham.notifshot.api.dto.CampaignResponse;
import com.ilham.notifshot.api.dto.CreateCampaignRequest;
import com.ilham.notifshot.application.notification.NotificationJobMessage;
import com.ilham.notifshot.application.notification.NotificationProducer;
import com.ilham.notifshot.domain.campaign.Campaign;
import com.ilham.notifshot.domain.campaign.CampaignStatus;
import com.ilham.notifshot.domain.notification.NotificationJob;
import com.ilham.notifshot.domain.notification.NotificationStatus;
import com.ilham.notifshot.domain.recipient.Recipient;
import com.ilham.notifshot.domain.tenant.Tenant;
import com.ilham.notifshot.repository.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final TenantRepository tenantRepository;
    private final RecipientRepository recipientRepository;
    private final NotificationJobRepository notificationJobRepository;
    private final NotificationProducer notificationProducer;

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request, MultipartFile csvFile)
            throws IOException, CsvValidationException {

        // Load and validate tenant
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + request.getTenantId()));

        if (!tenant.isActive()) {
            throw new IllegalStateException("Tenant is not active");
        }

        if (tenant.hasExceededCampaignLimit()) {
            throw new IllegalStateException("Tenant has exceeded monthly campaign limit");
        }

        // Create campaign
        Campaign campaign = Campaign.builder()
                .tenant(tenant)
                .name(request.getName())
                .channel(request.getChannel())
                .messageTemplate(request.getMessageTemplate())
                .status(request.getScheduledAt() != null ? CampaignStatus.SCHEDULED : CampaignStatus.PENDING)
                .scheduledAt(request.getScheduledAt())
                .transactional(request.isTransactional())
                .build();

        campaign = campaignRepository.save(campaign);
        final UUID campaignId = campaign.getId();

        log.info("Campaign created id={} tenant={} channel={}",
                campaignId, tenant.getId(), campaign.getChannel());

        // Stream CSV — never loads entire file into memory
        int recipientCount = 0;
        try (CSVReader reader = new CSVReader(new InputStreamReader(csvFile.getInputStream()))) {
            String[] headers = reader.readNext(); // skip header row
            String[] row;

            while ((row = reader.readNext()) != null) {
                if (row.length < 3) continue;

                String recipientId = row[0].trim();
                String email = row[1].trim();
                String phone = row[2].trim();
                String timezone = row.length > 3 ? row[3].trim() : "UTC";

                // Save recipient
                Recipient recipient = Recipient.builder()
                        .tenant(tenant)
                        .campaign(campaign)
                        .recipientId(recipientId)
                        .email(email.isEmpty() ? null : email)
                        .phone(phone.isEmpty() ? null : phone)
                        .timezone(timezone)
                        .build();

                recipient = recipientRepository.save(recipient);

                // Build idempotency key — guarantees no duplicate sends
                String idempotencyKey = String.format("%s:%s:%s",
                        campaignId, recipientId, campaign.getChannel());

                // Check for existing job — idempotency
                if (notificationJobRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
                    log.warn("Duplicate job detected for key={} — skipping", idempotencyKey);
                    continue;
                }

                // Create notification job
                NotificationJob job = NotificationJob.builder()
                        .tenant(tenant)
                        .campaign(campaign)
                        .recipient(recipient)
                        .channel(campaign.getChannel())
                        .status(NotificationStatus.PENDING)
                        .idempotencyKey(idempotencyKey)
                        .maxRetries(3)
                        .build();

                job = notificationJobRepository.save(job);

                // Enqueue to Kafka — async processing
                NotificationJobMessage message = NotificationJobMessage.builder()
                        .notificationJobId(job.getId())
                        .tenantId(tenant.getId())
                        .campaignId(campaignId)
                        .recipientId(recipient.getId())
                        .channel(campaign.getChannel())
                        .idempotencyKey(idempotencyKey)
                        .retryCount(0)
                        .build();

                final NotificationJobMessage finalMessage = message;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        notificationProducer.sendNotificationJob(finalMessage);
                    }
                });
                recipientCount++;
            }
        }

        // Update total recipients
        campaign.setTotalRecipients(recipientCount);
        campaign.setStatus(CampaignStatus.RUNNING);
        campaignRepository.save(campaign);

        // Update tenant usage
        tenant.setCampaignsUsed(tenant.getCampaignsUsed() + 1);
        tenant.setMessagesUsed(tenant.getMessagesUsed() + recipientCount);
        tenantRepository.save(tenant);

        log.info("Campaign id={} enqueued {} jobs to Kafka", campaignId, recipientCount);

        return toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> getCampaigns(UUID tenantId, Pageable pageable) {
        if (tenantId != null) {
            return campaignRepository.findByTenantId(tenantId, pageable)
                    .map(this::toResponse);
        }
        return campaignRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found: " + id));
        return toResponse(campaign);
    }

    @Transactional
    public int retryFailures(UUID campaignId) {
        campaignRepository.findById(campaignId)
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found: " + campaignId));

        var failedJobs = notificationJobRepository
                .findRetryableJobsByCampaignId(campaignId);

        for (NotificationJob job : failedJobs) {
            // Reset retry count and status so it can be processed fresh
            job.setRetryCount(0);
            job.setStatus(NotificationStatus.PENDING);
            notificationJobRepository.save(job);

            // Decrement failed count since we're retrying
            campaignRepository.decrementFailedCount(campaignId);

            NotificationJobMessage message = NotificationJobMessage.builder()
                    .notificationJobId(job.getId())
                    .tenantId(job.getTenant().getId())
                    .campaignId(campaignId)
                    .recipientId(job.getRecipient().getId())
                    .channel(job.getChannel())
                    .idempotencyKey(job.getIdempotencyKey())
                    .retryCount(0)
                    .build();

            notificationProducer.sendRetryJob(message);
            log.info("Retrying job={} campaign={}", job.getId(), campaignId);
        }

        return failedJobs.size();
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .tenantId(campaign.getTenant().getId())
                .name(campaign.getName())
                .channel(campaign.getChannel())
                .messageTemplate(campaign.getMessageTemplate())
                .status(campaign.getStatus())
                .totalRecipients(campaign.getTotalRecipients())
                .sentCount(campaign.getSentCount())
                .failedCount(campaign.getFailedCount())
                .skippedCount(campaign.getSkippedCount())
                .deliveryRate(campaign.getDeliveryRate())
                .scheduledAt(campaign.getScheduledAt())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}