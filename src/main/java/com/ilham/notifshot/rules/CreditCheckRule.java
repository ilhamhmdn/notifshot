package com.ilham.notifshot.rules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreditCheckRule implements NotificationRule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        if (context.getTenant().hasExceededCampaignLimit()) {
            log.warn("Tenant {} has exceeded campaign limit", context.getTenant().getId());
            return RuleResult.reject("Tenant has exceeded monthly campaign limit");
        }

        if (context.getTenant().hasExceededMessageLimit(context.getRecipientCount())) {
            log.warn("Tenant {} has exceeded message limit", context.getTenant().getId());
            return RuleResult.reject("Tenant has exceeded monthly message limit");
        }

        return RuleResult.allow();
    }

    @Override
    public int getOrder() {
        return 3;
    }
}