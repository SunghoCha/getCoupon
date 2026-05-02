package com.sungho.letterpick.newsletter.application.provided;

import com.sungho.letterpick.newsletter.domain.NewsletterCategory;

public record NewsletterSearchCondition(
        NewsletterCategory category
) {
}
