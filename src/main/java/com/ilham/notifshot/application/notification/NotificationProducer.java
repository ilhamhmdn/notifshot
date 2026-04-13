package com.ilham.notifshot.application.notification;

import com.ilham.notifshot.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationJobMessage> kafkaTemplate;

    public void sendNotificationJob(NotificationJobMessage message) {
        String key = message.getTenantId().toString();

        kafkaTemplate.send(KafkaConfig.TOPIC_NOTIFICATION_JOBS, key, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send notification job {} to Kafka: {}",
                                message.getNotificationJobId(), ex.getMessage());
                    } else {
                        log.debug("Notification job {} sent to partition {}",
                                message.getNotificationJobId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }

    public void sendRetryJob(NotificationJobMessage message) {
        String key = message.getTenantId().toString();

        kafkaTemplate.send(KafkaConfig.TOPIC_NOTIFICATION_RETRY, key, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send retry job {} to Kafka: {}",
                                message.getNotificationJobId(), ex.getMessage());
                    }
                });
    }
}