package com.sungho.letterpick.newsletter.adapter.seed;

import com.sungho.letterpick.common.domain.Email;
import com.sungho.letterpick.newsletter.adapter.persistence.NewslettersRepository;
import com.sungho.letterpick.newsletter.domain.Newsletter;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
@Profile("local")
@ConditionalOnProperty(name = "letterpick.seed.newsletter.local-test.enabled", havingValue = "true")
public class LocalNewsletterSeedRunner implements ApplicationRunner {

    private static final String LOCAL_TEST_NEWSLETTER_NAME = "로컬 테스트 뉴스레터";
    private static final String LOCAL_TEST_NEWSLETTER_DESCRIPTION = "로컬 메일 수신 E2E 테스트용 뉴스레터";
    private static final String LOCAL_TEST_NEWSLETTER_IMAGE_URL = "https://example.com/letterpick-local-test-newsletter.png";
    private static final NewsletterCategory LOCAL_TEST_NEWSLETTER_CATEGORY = NewsletterCategory.TECH;
    private static final String LOCAL_TEST_NEWSLETTER_SUBSCRIBE_URL = "https://example.com/subscribe";
    private static final String LOCAL_TEST_NEWSLETTER_MAIN_PAGE_URL = "https://example.com";
    private static final String LOCAL_TEST_NEWSLETTER_EMAIL = "tjdgh1129@gmail.com";

    private final NewslettersRepository newslettersRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (newslettersRepository.findByEmailAddress(LOCAL_TEST_NEWSLETTER_EMAIL).isPresent()) {
            log.info("로컬 테스트 Newsletter 시드 이미 적재됨. skip (email={})", LOCAL_TEST_NEWSLETTER_EMAIL);
            return;
        }

        Newsletter newsletter = Newsletter.register(
                LOCAL_TEST_NEWSLETTER_NAME,
                LOCAL_TEST_NEWSLETTER_DESCRIPTION,
                LOCAL_TEST_NEWSLETTER_IMAGE_URL,
                LOCAL_TEST_NEWSLETTER_CATEGORY,
                LOCAL_TEST_NEWSLETTER_SUBSCRIBE_URL,
                LOCAL_TEST_NEWSLETTER_MAIN_PAGE_URL,
                new Email(LOCAL_TEST_NEWSLETTER_EMAIL)
        );

        newslettersRepository.save(newsletter);
        log.info("로컬 테스트 Newsletter 시드 적재 완료. email={}", LOCAL_TEST_NEWSLETTER_EMAIL);
    }
}
