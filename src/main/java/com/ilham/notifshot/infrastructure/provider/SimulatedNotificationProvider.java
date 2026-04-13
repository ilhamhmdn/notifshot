package com.ilham.notifshot.infrastructure.provider;

import com.ilham.notifshot.domain.notification.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
public class SimulatedNotificationProvider {

    private static final Random RANDOM = new Random();
    private static final int MIN_LATENCY_MS = 50;
    private static final int MAX_LATENCY_MS = 200;
    private static final double FAILURE_RATE = 0.20;

    public ProviderResponse send(Channel channel, String recipient, String message, String tenantId) {
        simulateLatency();

        if (RANDOM.nextDouble() < FAILURE_RATE) {
            log.debug("Provider simulated failure for channel={} recipient={}",
                    channel, maskRecipient(recipient));
            return ProviderResponse.builder()
                    .success(false)
                    .message("Provider error: temporary failure")
                    .build();
        }

        // Log masked PII — GDPR compliant
        log.info("channel={} tenant={} recipient={} status=SENT",
                channel, tenantId, maskRecipient(recipient));

        return ProviderResponse.builder()
                .success(true)
                .message("Delivered")
                .providerMessageId(UUID.randomUUID().toString())
                .build();
    }

    private void simulateLatency() {
        try {
            int latency = MIN_LATENCY_MS + RANDOM.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS);
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Mask PII for GDPR/SOC2 compliance
    private String maskRecipient(String recipient) {
        if (recipient == null || recipient.length() < 4) return "***";
        if (recipient.contains("@")) {
            int atIndex = recipient.indexOf("@");
            return recipient.substring(0, 2) + "***" + recipient.substring(atIndex);
        }
        return recipient.substring(0, 3) + "***" + recipient.substring(recipient.length() - 2);
    }
}