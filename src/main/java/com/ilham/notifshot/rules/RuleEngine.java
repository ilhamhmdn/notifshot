package com.ilham.notifshot.rules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class RuleEngine {

    private final List<NotificationRule> rules;

    public RuleEngine(List<NotificationRule> rules) {
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(NotificationRule::getOrder))
                .toList();
    }

    public RuleResult evaluate(RuleContext context) {
        for (NotificationRule rule : rules) {
            RuleResult result = rule.evaluate(context);

            if (!result.isAllow()) {
                log.info("Rule {} blocked notification for recipient {} — action={} reason={}",
                        rule.getClass().getSimpleName(),
                        context.getRecipient().getRecipientId(),
                        result.getAction(),
                        result.getReason());
                return result;
            }
        }

        return RuleResult.allow();
    }
}