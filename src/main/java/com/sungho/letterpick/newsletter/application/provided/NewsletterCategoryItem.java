package com.sungho.letterpick.newsletter.application.provided;

import com.sungho.letterpick.newsletter.domain.NewsletterCategory;

import static java.util.Objects.requireNonNull;

public record NewsletterCategoryItem(
        String code,
        String label
) {
    public NewsletterCategoryItem {
        requireNonNull(code);
        requireNonNull(label);
    }

    public static NewsletterCategoryItem from(NewsletterCategory category) {
        requireNonNull(category);
        return new NewsletterCategoryItem(category.name(), category.label());
    }
}
