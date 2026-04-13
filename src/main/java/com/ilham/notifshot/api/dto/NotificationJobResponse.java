package com.ilham.notifshot.api.dto;

import com.ilham.notifshot.domain.notification.Channel;
import com.ilham.notifshot.domain.notification.NotificationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationJobResponse {
    private UUID id;
    private UUID campaignId;
    private UUID tenantId;
    private String recipientId;
    private Channel channel;
    private NotificationStatus status;
    private int retryCount;
    private LocalDateTime createdAt;
}