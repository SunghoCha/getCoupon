package com.sungho.letterpick.newsletter.application.provided;

import java.time.Instant;

public record NewsletterIssueSearchCondition(
        Instant receivedFrom,
        Instant receivedTo
) {
    public static NewsletterIssueSearchCondition empty() {
        return new NewsletterIssueSearchCondition(null, null);
    }

    public static NewsletterIssueSearchCondition receivedAtRange(Instant receivedFrom, Instant receivedTo) {
        return new NewsletterIssueSearchCondition(receivedFrom, receivedTo);
    }
}
