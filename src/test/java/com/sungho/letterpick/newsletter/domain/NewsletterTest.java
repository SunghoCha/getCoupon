package com.sungho.letterpick.newsletter.domain;

import com.sungho.letterpick.common.domain.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsletterTest {

    @Test
    @DisplayName("유효한 정보로 뉴스레터를 등록할 수 있다")
    void register() {
        Newsletter newsletter = Newsletter.register(
                "테스트 뉴스레터",
                "테스트용 설명",
                "https://example.com/test.png",
                NewsletterCategory.TECH,
                "https://example.com/subscribe",
                "https://example.com",
                new Email("test@newsletter.example.com")
        );

        assertThat(newsletter.getName()).isEqualTo("테스트 뉴스레터");
        assertThat(newsletter.getDescription()).isEqualTo("테스트용 설명");
        assertThat(newsletter.getImageUrl()).isEqualTo("https://example.com/test.png");
        assertThat(newsletter.getCategory()).isEqualTo(NewsletterCategory.TECH);
        assertThat(newsletter.getSubscribeUrl()).isEqualTo("https://example.com/subscribe");
        assertThat(newsletter.getMainPageUrl()).isEqualTo("https://example.com");
        assertThat(newsletter.getEmail()).isEqualTo(new Email("test@newsletter.example.com"));
    }
}
