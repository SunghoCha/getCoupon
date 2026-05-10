package com.sungho.letterpick.newsletter.application.provided;

import com.sungho.letterpick.newsletter.domain.NewsletterCategory;

import static java.util.Objects.requireNonNull;

public record NewsletterListItem(
        Long newsletterId,
        String name,
        String description,
        String imageUrl,
        NewsletterCategoryItem category
) {

    public NewsletterListItem {
        requireNonNull(newsletterId);
        requireNonNull(name);
        requireNonNull(description);
        requireNonNull(imageUrl);
        requireNonNull(category);
    }

    public NewsletterListItem(
            Long newsletterId,
            String name,
            String description,
            String imageUrl,
            NewsletterCategory category
    ) {
        this(newsletterId, name, description, imageUrl, NewsletterCategoryItem.from(category));
    }
}
