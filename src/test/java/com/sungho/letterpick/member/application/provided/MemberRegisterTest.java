package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.LetterPickTestConfiguration;
import com.sungho.letterpick.member.application.required.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberStatus;
import com.sungho.letterpick.member.domain.exception.DuplicateEmailException;
import com.sungho.letterpick.member.domain.exception.DuplicateNicknameException;
import com.sungho.letterpick.member.domain.exception.MemberErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(LetterPickTestConfiguration.class)
class MemberRegisterTest {

    @Autowired
    MemberRegister memberRegister;
    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void cleanUp() {
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("회원 가입이 정상적으로 이루어진다")
    void register() {
        // given
        MemberRegisterRequest request = new MemberRegisterRequest("email@test.com", "nickname");
        // when
        Member member = memberRegister.register(request);
        // then
        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getEmail().address()).isEqualTo("email@test.com");
        assertThat(member.getNickname().name()).isEqualTo("nickname");

    }

    @Test
    @DisplayName("회원 가입 시 이메일이 중복이면 실패한다")
    void registerFailsWhenEmailDuplicated() {
        // given
        MemberRegisterRequest request = new MemberRegisterRequest("email@test.com", "nickname");
        MemberRegisterRequest otherRequest = new MemberRegisterRequest("email@test.com", "other");
        memberRegister.register(request);
        // then
        Assertions.assertThatThrownBy(() -> memberRegister.register(otherRequest))
                .isInstanceOf(DuplicateEmailException.class);

    }

    @Test
    @DisplayName("회원 가입 시 닉네임이 중복이면 실패한다")
    void registerFailsWhenNicknameDuplicated() {
        // given
        MemberRegisterRequest request = new MemberRegisterRequest("email@test.com", "nickname");
        MemberRegisterRequest otherRequest = new MemberRegisterRequest("other@test.com", "nickname");
        // when
        memberRegister.register(request);
        // then
        Assertions.assertThatThrownBy(() -> memberRegister.register(otherRequest))
                .isInstanceOf(DuplicateNicknameException.class)
                .hasMessageContaining(MemberErrorCode.DUPLICATE_NICKNAME.getMessage());
    }


}
