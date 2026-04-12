package com.sungho.letterpick.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    @DisplayName("유효한 이메일 형식으로 생성할 수 있다")
    void createWithValidEmail() {
        Email email = new Email("test@example.com");
    }

    @Test
    @DisplayName("잘못된 이메일 형식이면 생성 실패한다")
    void createWithInvalidEmail() {
        Assertions.assertThatThrownBy(() -> new Email("invalid")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 이메일이면 생성 실패한다")
    void createEmailWithNull() {
        // then
        Assertions.assertThatThrownBy(() -> new Email(null)).isInstanceOf( NullPointerException.class);
    }
}