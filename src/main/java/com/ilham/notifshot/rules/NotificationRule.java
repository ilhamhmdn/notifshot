package com.ilham.notifshot.rules;

public interface NotificationRule {
    RuleResult evaluate(RuleContext context);
    int getOrder();
}