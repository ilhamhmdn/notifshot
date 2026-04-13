package com.ilham.notifshot.rules;

import com.ilham.notifshot.repository.SuppressionListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalSuppressionRule implements NotificationRule {

    private final SuppressionListRepository suppressionListRepository;

    @Override
    public RuleResult evaluate(RuleContext context) {
        String recipientId = context.getRecipient().getRecipientId();
        boolean isSuppressed = suppressionListRepository
                .existsByRecipientIdAndChannel(recipientId, context.getChannel());

        if (isSuppressed) {
            log.info("Recipient {} is suppressed for channel {}", recipientId, context.getChannel());
            return RuleResult.skip("Recipient unsubscribed from " + context.getChannel());
        }

        return RuleResult.allow();
    }

    @Override
    public int getOrder() {
        return 1;
    }
}