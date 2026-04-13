package com.ilham.notifshot.rules;

import lombok.Getter;

@Getter
public class RuleResult {

    public enum Action {
        ALLOW, SKIP, DELAY, REJECT, DISCARD
    }

    private final Action action;
    private final String reason;

    private RuleResult(Action action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public static RuleResult allow() {
        return new RuleResult(Action.ALLOW, null);
    }

    public static RuleResult skip(String reason) {
        return new RuleResult(Action.SKIP, reason);
    }

    public static RuleResult delay(String reason) {
        return new RuleResult(Action.DELAY, reason);
    }

    public static RuleResult reject(String reason) {
        return new RuleResult(Action.REJECT, reason);
    }

    public static RuleResult discard(String reason) {
        return new RuleResult(Action.DISCARD, reason);
    }

    public boolean isAllow() {
        return action == Action.ALLOW;
    }
}