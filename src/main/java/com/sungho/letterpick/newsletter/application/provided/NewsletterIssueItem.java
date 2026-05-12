package com.sungho.letterpick.newsletter.application.provided;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record NewsletterIssueItem(
        Long issueId,
        Long newsletterId,
        String newsletterName,
        String newsletterImageUrl,
        String subject,
        String previewText,
        Instant receivedAt,
        boolean read
) {

    public NewsletterIssueItem {
        requireNonNull(issueId);
        requireNonNull(newsletterId);
        requireNonNull(newsletterName);
        requireNonNull(newsletterImageUrl);
        requireNonNull(subject);
        requireNonNull(previewText);
        requireNonNull(receivedAt);
    }
}
