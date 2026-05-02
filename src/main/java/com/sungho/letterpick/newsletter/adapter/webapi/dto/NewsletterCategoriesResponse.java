package com.sungho.letterpick.newsletter.adapter.webapi.dto;

import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryItem;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record NewsletterCategoriesResponse(
        List<CategoryResponse> categories
) {
    public NewsletterCategoriesResponse {
        categories = List.copyOf(requireNonNull(categories));
    }

    public static NewsletterCategoriesResponse from(List<NewsletterCategoryItem> categories) {
        requireNonNull(categories);
        return new NewsletterCategoriesResponse(
                categories.stream()
                        .map(CategoryResponse::from)
                        .toList()
        );
    }

    public record CategoryResponse(
            String code,
            String label
    ) {
        public CategoryResponse {
            requireNonNull(code);
            requireNonNull(label);
        }

        public static CategoryResponse from(NewsletterCategoryItem category) {
            requireNonNull(category);
            return new CategoryResponse(category.code(), category.label());
        }
    }
}
