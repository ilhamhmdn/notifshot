package com.ilham.notifshot.rules;

import com.ilham.notifshot.domain.campaign.Campaign;
import com.ilham.notifshot.domain.notification.Channel;
import com.ilham.notifshot.domain.recipient.Recipient;
import com.ilham.notifshot.domain.tenant.Tenant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RuleContext {
    private final Tenant tenant;
    private final Campaign campaign;
    private final Recipient recipient;
    private final Channel channel;
    private final int recipientCount;
}