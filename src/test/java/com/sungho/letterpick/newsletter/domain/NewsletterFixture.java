package com.sungho.letterpick.newsletter.domain;

import com.sungho.letterpick.common.domain.Email;

public class NewsletterFixture {

    public static Newsletter createNewsletter() {
        return createNewsletter("테스트 뉴스레터", NewsletterCategory.TECH);
    }

    public static Newsletter createNewsletter(String title, NewsletterCategory category) {
        return Newsletter.register(
                title,
                "테스트용 설명",
                "https://example.com/test.png",
                category,
                "https://example.com/subscribe",
                "https://example.com",
                new Email("test-" + Integer.toUnsignedString(title.hashCode(), 36) + "@newsletter.example.com")
        );
    }
}
