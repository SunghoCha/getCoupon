package com.sungho.letterpick.member.domain;

import com.sungho.letterpick.member.domain.exception.MemberStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    @DisplayName("register() 후 상태는 ACTIVE이다")
    void registerSetsStatusActive() {
        // when
        Member member = Member.register(
                new Email("test@example.com"),
                new Nickname("테스트유저"));
        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("register() 후 email, nickname이 세팅된다")
    void registerSetsEmailAndNickName() {
        // when
        Member member = Member.register(
                new Email("test@example.com"),
                new Nickname("테스트유저"));

        // then
        assertThat(member.getEmail()).isEqualTo(new Email("test@example.com"));
        assertThat(member.getNickname()).isEqualTo(new Nickname("테스트유저"));
    }

    @Test
    @DisplayName("ACTIVE 회원은 닉네임을 변경할 수 있다")
    void changeNicknameWhenActive() {
        // given
        Member member = MemberFixture.createMember();
        // when
        member.changeNickname(new Nickname("수정된닉네임"));
        // then
        assertThat(member.getNickname()).isEqualTo(new Nickname("수정된닉네임"));
    }

    @Test
    @DisplayName("ACTIVE가 아닌 상태에서 닉네임 변경하면 실패한다")
    void changeNicknameFailsWhenNotActive() {
        // given
        Member member = MemberFixture.createMember();
        member.suspend();
        // then
        assertThatThrownBy(() -> {member.changeNickname(new Nickname("수정된닉네임"));
        }).isInstanceOf(MemberStatusException.class);
    }

    @Test
    @DisplayName("ACTIVE 회원을 정지하면 SUSPENDED가 된다")
    void suspendWhenActive() {
        // given
        Member member = MemberFixture.createMember();
        // when
        member.suspend();
        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
    }


    @Test
    @DisplayName("ACTIVE가 아닌 상태에서 정지하면 실패한다")
    void suspendFailsWhenNotActive() {
        // given
        Member member = MemberFixture.createMember();
        member.suspend();
        // then
        assertThatThrownBy(member::suspend)
                .isInstanceOf(MemberStatusException.class);

    }

    @Test
    @DisplayName("ACTIVE 회원이 본인 ID로 탈퇴하면 DEACTIVATED가 된다")
    void withdrawByOwner() {
        // given
        Member member = MemberFixture.createMember(1L);
        // when
        member.withdraw();
        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
    }


    @Test
    @DisplayName("ACTIVE가 아닌 상태에서 본인 탈퇴하면 실패한다")
    void withdrawFailsWhenNotActive() {
        // given
        Member member = MemberFixture.createMember(1L);
        member.suspend();
        // then
        assertThatThrownBy(member::withdraw)
                .isInstanceOf(MemberStatusException .class);
    }

    @Test
    @DisplayName("SUSPENDED 회원을 관리자가 탈퇴 처리하면 DEACTIVATED가 된다")
    void withdrawByAdmin() {
        // given
        Member member = MemberFixture.createMember();
        member.suspend();
        // when
        member.withdrawByAdmin();
        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
    }

    @Test
    @DisplayName("SUSPENDED가 아닌 상태에서 관리자가 탈퇴 처리하면 실패한다")
    void withdrawByAdminFailsWhenNotSuspended() {
        // given
        Member member = MemberFixture.createMember();
        // then
        assertThatThrownBy(member::withdrawByAdmin).isInstanceOf(MemberStatusException.class);

    }
}