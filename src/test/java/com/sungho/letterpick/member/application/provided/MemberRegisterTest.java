package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.LetterPickTestConfiguration;
import com.sungho.letterpick.member.application.required.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberFixture;
import com.sungho.letterpick.member.domain.MemberStatus;
import com.sungho.letterpick.member.domain.Nickname;
import com.sungho.letterpick.member.domain.exception.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> memberRegister.register(otherRequest))
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
        assertThatThrownBy(() -> memberRegister.register(otherRequest))
                .isInstanceOf(DuplicateNicknameException.class)
                .hasMessageContaining(MemberErrorCode.DUPLICATE_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("회원 닉네임 수정이 정상적으로 이루어진다")
    void changeNickname() {
        // given
        Member member = MemberFixture.createMember();
        Member savedMember = memberRepository.save(member);
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest(savedMember.getId(), "수정된닉네임");
        // when
        memberRegister.changeNickname(request);

        // then
        Member changedMember = memberRepository.findById(savedMember.getId()).orElseThrow();
        assertThat(changedMember.getNickname()).isEqualTo(new Nickname(request.nickname()));

    }

    @Test
    @DisplayName("존재하지 않는 회원의 닉네임을 변경하면 실패한다")
    void changeNicknameFailsWhenMemberNotFound() {
        // given
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest(1L, "수정된닉네임");
        // then
        assertThatThrownBy(() -> memberRegister.changeNickname(request))
                .isInstanceOf(MemberNotFoundException.class);

    }

    @Test
    @DisplayName("다른 회원이 사용 중인 닉네임으로 변경하면 실패한다")
    void changeNicknameFailsWhenNicknameDuplicated() {
        // given
        Member member = memberRepository.save(MemberFixture.createMember("test1@email.com", "닉네임1"));
        Member otherMember = memberRepository.save(MemberFixture.createMember("test2@email.com", "닉네임2"));
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest(otherMember.getId(), member.getNickname().name());
        // then
        assertThatThrownBy(() -> memberRegister.changeNickname(request))
                .isInstanceOf(DuplicateNicknameException.class);

    }

    @Test
    @DisplayName("현재와 동일한 닉네임으로 변경하면 변경 없이 정상 처리된다")
    void changeNicknameKeepsWhenSameNickname() {
        // given
        Member member = memberRepository.save(MemberFixture.createMember("test1@email.com", "닉네임1"));
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest(member.getId(), member.getNickname().name());
        // when
        memberRegister.changeNickname(request);
        // then
        Member savedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(savedMember.getNickname()).isEqualTo(member.getNickname());

    }

    @Test
    @DisplayName("ACTIVE 아닌 회원이 닉네임을 변경하면 실패한다")
    void changeNicknameFailsWhenNotActive() {
        // given
        Member member = MemberFixture.createMember();
        member.suspend();
        Member savedMember = memberRepository.save(member);
        MemberNicknameChangeRequest request =
                new MemberNicknameChangeRequest(savedMember.getId(), "새로운닉네임");
        // when
        assertThatThrownBy(() -> memberRegister.changeNickname(request)).isInstanceOf(MemberStatusException.class);
        
    }

    @Test
    @DisplayName("회원 탈퇴가 정상적으로 이루어진다")
    void withdraw() {
        // given
        Member savedMember = memberRepository.save(MemberFixture.createMember());
        // when
        memberRegister.withdraw(savedMember.getId());
        // then
        Member foundMember = memberRepository.findById(savedMember.getId()).orElseThrow();
        assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
    }

    @Test
    @DisplayName("존재하지 않는 회원이 탈퇴하면 실패한다")
    void withdrawFailsWhenMemberNotFound() {
        // then
        assertThatThrownBy(() -> memberRegister.withdraw(1L)).isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("ACTIVE 아닌 회원이 탈퇴하면 실패한다")
    void withdrawFailsWhenNotActive() {
        // given
        Member member = MemberFixture.createMember();
        member.suspend();
        Member savedMember = memberRepository.save(member);

        // then
        assertThatThrownBy(() -> memberRegister.withdraw(savedMember.getId()))
                .isInstanceOf(MemberStatusException.class);

    }
    

}
