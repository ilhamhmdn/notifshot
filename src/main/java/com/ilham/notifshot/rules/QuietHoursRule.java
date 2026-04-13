package com.ilham.notifshot.rules;

import com.ilham.notifshot.domain.notification.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Component
public class QuietHoursRule implements NotificationRule {

    @Value("${app.notification.quiet-hours.start:22}")
    private int quietStart;

    @Value("${app.notification.quiet-hours.end:8}")
    private int quietEnd;

    @Override
    public RuleResult evaluate(RuleContext context) {
        // Email is not subject to quiet hours
        if (context.getChannel() == Channel.EMAIL) {
            return RuleResult.allow();
        }

        // Transactional messages (OTP etc) bypass quiet hours
        if (context.getCampaign().isTransactional()) {
            return RuleResult.allow();
        }

        String timezone = context.getRecipient().getTimezone();
        LocalTime now;
        try {
            now = LocalTime.now(ZoneId.of(timezone));
        } catch (Exception e) {
            now = LocalTime.now(ZoneId.of("UTC"));
        }

        int hour = now.getHour();
        boolean isQuietHours = hour >= quietStart || hour < quietEnd;

        if (isQuietHours) {
            log.info("Recipient {} is in quiet hours ({}). Delaying.",
                    context.getRecipient().getRecipientId(), timezone);
            return RuleResult.delay("Outside social hours (10pm-8am) in timezone " + timezone);
        }

        return RuleResult.allow();
    }

    @Override
    public int getOrder() {
        return 2;
    }
}