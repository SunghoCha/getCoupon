package com.sungho.letterpick.newsletter.adapter.webapi.dto;

import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record NewsletterIssuesResponse(
        List<NewsletterIssueResponse> items,
        PageResponse page
) {

    public NewsletterIssuesResponse {
        items = List.copyOf(requireNonNull(items));
        requireNonNull(page);
    }

    public static NewsletterIssuesResponse from(Slice<NewsletterIssueItem> issues) {
        requireNonNull(issues);

        return new NewsletterIssuesResponse(
                issues.getContent().stream()
                        .map(NewsletterIssueResponse::from)
                        .toList(),
                PageResponse.from(issues)
        );
    }

    public record NewsletterIssueResponse(
            Long issueId,
            Long newsletterId,
            String newsletterName,
            String newsletterImageUrl,
            String subject,
            String previewText,
            Instant receivedAt,
            boolean read
    ) {
        public static NewsletterIssueResponse from(NewsletterIssueItem newsletterIssue) {
            requireNonNull(newsletterIssue);

            return new NewsletterIssueResponse(
                    newsletterIssue.issueId(),
                    newsletterIssue.newsletterId(),
                    newsletterIssue.newsletterName(),
                    newsletterIssue.newsletterImageUrl(),
                    newsletterIssue.subject(),
                    newsletterIssue.previewText(),
                    newsletterIssue.receivedAt(),
                    newsletterIssue.read()
            );
        }
    }
}
