package com.sungho.letterpick.newsletter.adapter.seed;

import com.sungho.letterpick.common.domain.Email;
import com.sungho.letterpick.newsletter.adapter.persistence.NewslettersRepository;
import com.sungho.letterpick.newsletter.domain.Newsletter;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalNewsletterSeedRunnerTest {

    private static final String LOCAL_TEST_NEWSLETTER_EMAIL = "tjdgh1129@gmail.com";

    @InjectMocks
    private LocalNewsletterSeedRunner runner;

    @Mock
    private NewslettersRepository newslettersRepository;

    @Test
    @DisplayName("로컬 테스트 뉴스레터가 없으면 저장한다")
    void run_saves_local_test_newsletter_when_missing() {
        // given
        given(newslettersRepository.findByEmailAddress(LOCAL_TEST_NEWSLETTER_EMAIL))
                .willReturn(Optional.empty());

        // when
        runner.run(null);

        // then
        ArgumentCaptor<Newsletter> newsletterCaptor = ArgumentCaptor.forClass(Newsletter.class);
        verify(newslettersRepository).save(newsletterCaptor.capture());

        Newsletter newsletter = newsletterCaptor.getValue();
        assertThat(newsletter.getEmail().address()).isEqualTo(LOCAL_TEST_NEWSLETTER_EMAIL);
        assertThat(newsletter.getName()).isEqualTo("로컬 테스트 뉴스레터");
    }

    @Test
    @DisplayName("로컬 테스트 뉴스레터가 이미 있으면 저장하지 않는다")
    void run_skips_when_local_test_newsletter_exists() {
        // given
        Newsletter existingNewsletter = Newsletter.register(
                "로컬 테스트 뉴스레터",
                "로컬 메일 수신 E2E 테스트용 뉴스레터",
                "https://example.com/letterpick-local-test-newsletter.png",
                NewsletterCategory.TECH,
                "https://example.com/subscribe",
                "https://example.com",
                new Email(LOCAL_TEST_NEWSLETTER_EMAIL)
        );
        given(newslettersRepository.findByEmailAddress(LOCAL_TEST_NEWSLETTER_EMAIL))
                .willReturn(Optional.of(existingNewsletter));

        // when
        runner.run(null);

        // then
        verify(newslettersRepository, never()).save(any());
    }
}
