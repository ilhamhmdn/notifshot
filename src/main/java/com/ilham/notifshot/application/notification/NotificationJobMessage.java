package com.ilham.notifshot.application.notification;

import com.ilham.notifshot.domain.notification.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJobMessage {
    private UUID notificationJobId;
    private UUID tenantId;
    private UUID campaignId;
    private UUID recipientId;
    private Channel channel;
    private String idempotencyKey;
    private int retryCount;
}