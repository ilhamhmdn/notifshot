package com.ilham.notifshot.rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeduplicationRule implements NotificationRule {

    private final StringRedisTemplate redisTemplate;

    // 5 minute deduplication window
    private static final Duration DEDUP_TTL = Duration.ofMinutes(5);

    @Override
    public RuleResult evaluate(RuleContext context) {
        // Don't deduplicate retries — only check fresh sends
        if (context.getRetryCount() > 0) {
            return RuleResult.allow();
        }

        String key = buildDedupKey(context);
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", DEDUP_TTL);

        if (Boolean.FALSE.equals(isNew)) {
            log.info("Duplicate message detected for campaign {} recipient {}",
                    context.getCampaign().getId(),
                    context.getRecipient().getRecipientId());
            return RuleResult.discard("Exact message sent in last 5 minutes");
        }

        return RuleResult.allow();
    }

    private String buildDedupKey(RuleContext context) {
        return String.format("dedup:%s:%s:%s",
                context.getCampaign().getId(),
                context.getRecipient().getRecipientId(),
                context.getCampaign().getMessageTemplate().hashCode());
    }

    @Override
    public int getOrder() {
        return 4;
    }
}