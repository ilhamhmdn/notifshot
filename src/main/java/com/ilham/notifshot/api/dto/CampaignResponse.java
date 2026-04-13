package com.ilham.notifshot.api.dto;

import com.ilham.notifshot.domain.campaign.CampaignStatus;
import com.ilham.notifshot.domain.notification.Channel;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CampaignResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private Channel channel;
    private String messageTemplate;
    private CampaignStatus status;
    private int totalRecipients;
    private int sentCount;
    private int failedCount;
    private int skippedCount;
    private double deliveryRate;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}