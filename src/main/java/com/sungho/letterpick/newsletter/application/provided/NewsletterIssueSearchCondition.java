package com.sungho.letterpick.newsletter.application.provided;

import java.time.Instant;

public record NewsletterIssueSearchCondition(
        Instant receivedFrom,
        Instant receivedTo,
        String keyword
) {
    public static NewsletterIssueSearchCondition empty() {
        return new NewsletterIssueSearchCondition(null, null, null);
    }

    public static NewsletterIssueSearchCondition withKeyword(String keyword) {
        return new NewsletterIssueSearchCondition(null, null, keyword);
    }

    public static NewsletterIssueSearchCondition receivedAtRange(Instant receivedFrom, Instant receivedTo) {
        return new NewsletterIssueSearchCondition(receivedFrom, receivedTo, null);
    }
}
