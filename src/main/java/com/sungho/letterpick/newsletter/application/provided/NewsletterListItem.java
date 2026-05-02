package com.sungho.letterpick.newsletter.application.provided;

import com.sungho.letterpick.newsletter.domain.NewsletterCategory;

import static java.util.Objects.requireNonNull;

public record NewsletterListItem(
        Long newsletterId,
        String name,
        String description,
        String imageUrl,
        NewsletterCategoryItem category,
        String memberNewsletterStatus
) {

    public NewsletterListItem {
        requireNonNull(newsletterId);
        requireNonNull(name);
        requireNonNull(category);
    }

    public NewsletterListItem(
            Long newsletterId,
            String name,
            String description,
            NewsletterCategory category
    ) {
        this(
                newsletterId,
                name,
                description,
                null,
                NewsletterCategoryItem.from(category),
                null
        );
    }
}
