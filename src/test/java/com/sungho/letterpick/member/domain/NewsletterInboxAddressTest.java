package com.sungho.letterpick.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewsletterInboxAddressTest {

    @Test
    @DisplayName("12자 lowercase 영문/숫자 토큰과 도메인으로 생성할 수 있다")
    void createWithValidAddress() {
        NewsletterInboxAddress newsletterInboxAddress =
                new NewsletterInboxAddress("abcd1234efgh@inbound.letterpick.test");

        assertThat(newsletterInboxAddress.address()).isEqualTo("abcd1234efgh@inbound.letterpick.test");
    }

    @Test
    @DisplayName("토큰 형식이 올바르지 않으면 생성 실패한다")
    void createFailsWhenTokenInvalid() {
        assertThatThrownBy(() -> new NewsletterInboxAddress("abc123@inbound.letterpick.test"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NewsletterInboxAddress("ABCD1234EFGH@inbound.letterpick.test"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NewsletterInboxAddress("abcd1234efg_@inbound.letterpick.test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("도메인 형식이 올바르지 않으면 생성 실패한다")
    void createFailsWhenDomainInvalid() {
        assertThatThrownBy(() -> new NewsletterInboxAddress("abcd1234efgh@localhost"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NewsletterInboxAddress("abcd1234efgh@"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 주소이면 생성 실패한다")
    void createFailsWhenNull() {
        assertThatThrownBy(() -> new NewsletterInboxAddress(null))
                .isInstanceOf(NullPointerException.class);
    }
}
