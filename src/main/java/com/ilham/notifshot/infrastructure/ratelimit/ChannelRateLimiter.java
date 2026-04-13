package com.ilham.notifshot.infrastructure.ratelimit;

import com.ilham.notifshot.domain.notification.Channel;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChannelRateLimiter {

    @Value("${app.notification.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(Channel channel) {
        Bucket bucket = buckets.computeIfAbsent(
                channel.name(), k -> createBucket()
        );

        boolean consumed = bucket.tryConsume(1);
        if (!consumed) {
            log.warn("Rate limit exceeded for channel={}", channel);
        }
        return consumed;
    }

    public void consumeBlocking(Channel channel) throws InterruptedException {
        Bucket bucket = buckets.computeIfAbsent(
                channel.name(), k -> createBucket()
        );
        bucket.asBlocking().consume(1);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}