package com.sungho.letterpick.member.application;

import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsletterInboxAddressGeneratorTest {

    @Test
    @DisplayName("설정된 도메인으로 12자 lowercase 영문/숫자 수신 주소를 생성한다")
    void generate() {
        NewsletterInboxAddressGenerator generator =
                new NewsletterInboxAddressGenerator("inbound.letterpick.test");

        NewsletterInboxAddress newsletterInboxAddress = generator.generate();

        assertThat(newsletterInboxAddress.address())
                .matches("^[a-z0-9]{12}@inbound\\.letterpick\\.test$");
    }
}
