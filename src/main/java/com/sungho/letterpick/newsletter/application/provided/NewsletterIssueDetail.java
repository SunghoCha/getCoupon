package com.sungho.letterpick.newsletter.application.provided;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record NewsletterIssueDetail(
        Long issueId,
        Long newsletterId,
        String newsletterName,
        String newsletterImageUrl,
        String subject,
        String content,
        Instant receivedAt,
        boolean read
) {

    public NewsletterIssueDetail {
        requireNonNull(issueId);
        requireNonNull(newsletterId);
        requireNonNull(newsletterName);
        requireNonNull(newsletterImageUrl);
        requireNonNull(subject);
        requireNonNull(content);
        requireNonNull(receivedAt);
    }

}
