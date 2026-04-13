package com.ilham.notifshot.api.dto;

import com.ilham.notifshot.domain.notification.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateCampaignRequest {

    @NotNull
    private UUID tenantId;

    @NotBlank
    private String name;

    @NotNull
    private Channel channel;

    @NotBlank
    private String messageTemplate;

    private LocalDateTime scheduledAt;

    private boolean transactional;
}