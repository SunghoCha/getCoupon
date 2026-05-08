package com.sungho.letterpick.newsletter.adapter.seed;

public record NewsletterSeed(
        String name,
        String description,
        String letterPickCategory,
        String email,
        String imageUrl,
        String mainPageUrl,
        String subscribeUrl
) {
}
