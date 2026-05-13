package com.sungho.letterpick.newsletter.adapter.webapi.dto;

import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueDetail;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record NewsletterIssueDetailResponse(
        Long issueId,
        Long newsletterId,
        String newsletterName,
        String newsletterImageUrl,
        String subject,
        String content,
        Instant receivedAt,
        boolean read
) {

    public NewsletterIssueDetailResponse {
        requireNonNull(issueId);
        requireNonNull(newsletterId);
        requireNonNull(newsletterName);
        requireNonNull(newsletterImageUrl);
        requireNonNull(subject);
        requireNonNull(content);
        requireNonNull(receivedAt);
    }

    public static NewsletterIssueDetailResponse from(NewsletterIssueDetail detail) {
        requireNonNull(detail);

        return new NewsletterIssueDetailResponse(
                detail.issueId(),
                detail.newsletterId(),
                detail.newsletterName(),
                detail.newsletterImageUrl(),
                detail.subject(),
                detail.content(),
                detail.receivedAt(),
                detail.read()
        );
    }

}
