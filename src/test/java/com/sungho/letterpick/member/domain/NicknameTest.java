package com.sungho.letterpick.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NicknameTest {

    @Test
    @DisplayName("유효한 닉네임으로 생성할 수 있다")
    void createWithValidNickname() {

        Nickname nickname = new Nickname("정상이름");
    }

    @Test
    @DisplayName("길이/특수문자 위반이면 생성 실패한다")
    void createWithInvalidNickname() {

        Assertions.assertThatThrownBy(() -> new Nickname("aaaaaaaaaaaaaaaaaaaaaaaaa")).isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> new Nickname("!#$")).isInstanceOf(IllegalArgumentException.class);

    }
    
    @Test
    @DisplayName("null 닉네임이면 생성 실패한다")
    void createNicknameWithNull() {
        // then
        Assertions.assertThatThrownBy(() -> new Nickname(null)).isInstanceOf( NullPointerException.class);
    }

}