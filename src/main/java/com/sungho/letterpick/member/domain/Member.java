package com.sungho.letterpick.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Nickname nickname;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private Member(Email email, Nickname nickname) {
        this.email = email;
        this.nickname = nickname;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member register(Email email, Nickname nickname) {
        return new Member(email, nickname);
    }

    public void changeNickname(Nickname nickname) {
        if (this.status != MemberStatus.ACTIVE) { // TODO : 커스텀예외
            throw new IllegalStateException("ACTIVE 회원만 닉네임을 변경할 수 있습니다.");
        }
        this.nickname = nickname;
    }

    public void suspend() {
        if (this.status != MemberStatus.ACTIVE) { // TODO : 커스텀예외
            throw new IllegalStateException("ACTIVE 상태에서만 정지할 수 있습니다.");
        }
        this.status = MemberStatus.SUSPENDED;
    }

    public void withdraw(Long requesterId) {
        if (!this.id.equals(requesterId)) {
            throw new IllegalArgumentException("본인만 탈퇴할 수 있습니다.");
        }
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태에서만 탈퇴할 수 있습니다.");
        }
        this.status = MemberStatus.DEACTIVATED;
    }

    public void withdrawByAdmin() {
        if (this.status != MemberStatus.SUSPENDED) {
            throw new IllegalStateException("SUSPENDED 상태에서만 관리자 탈퇴 처리할 수 있습니다.");
        }
        this.status = MemberStatus.DEACTIVATED;
    }
}
