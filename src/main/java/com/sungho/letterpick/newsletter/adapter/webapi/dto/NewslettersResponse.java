package com.sungho.letterpick.newsletter.adapter.webapi.dto;

import com.sungho.letterpick.newsletter.application.provided.NewsletterListItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryItem;
import org.springframework.data.domain.Slice;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record NewslettersResponse(
        List<NewsletterResponse> items,
        PageResponse page
) {

    public NewslettersResponse {
        items = List.copyOf(requireNonNull(items));
        requireNonNull(page);
    }

    public static NewslettersResponse from(Slice<NewsletterListItem> newsletters) {
        requireNonNull(newsletters);

        return new NewslettersResponse(
                newsletters.getContent().stream()
                        .map(NewsletterResponse::from)
                        .toList(),
                new PageResponse(
                        newsletters.getNumber(),
                        newsletters.getSize(),
                        newsletters.hasNext()
                )
        );
    }

    public record NewsletterResponse(
            Long newsletterId,
            String name,
            String description,
            String imageUrl,
            CategoryResponse category,
            String memberNewsletterStatus
    ) {
        public static NewsletterResponse from(NewsletterListItem newsletter) {
            requireNonNull(newsletter);

            return new NewsletterResponse(
                    newsletter.newsletterId(),
                    newsletter.name(),
                    newsletter.description(),
                    newsletter.imageUrl(),
                    CategoryResponse.from(newsletter.category()),
                    newsletter.memberNewsletterStatus()
            );
        }
    }

    public record CategoryResponse(
            String code,
            String label
    ) {
        public static CategoryResponse from(NewsletterCategoryItem category) {
            requireNonNull(category);
            return new CategoryResponse(category.code(), category.label());
        }
    }

    public record PageResponse(
            int number,
            int size,
            boolean hasNext
    ) {
    }
}
