package com.sungho.letterpick.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static java.util.Objects.requireNonNull;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "address",
            column = @Column(name = "email", nullable = false))
    private Email email;

    @Embedded
    @AttributeOverride(name = "name",
            column = @Column(name = "nickname", nullable = false))
    private Nickname nickname;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    private Member(Email email, Nickname nickname) {
        this.email = requireNonNull(email);
        this.nickname = requireNonNull(nickname);
        this.status = MemberStatus.ACTIVE;
    }

    public static Member register(Email email, Nickname nickname) {
        return new Member(email, nickname);
    }

    public void changeNickname(Nickname nickname) {
        if (this.status != MemberStatus.ACTIVE) { // TODO : 커스텀예외
            throw new IllegalStateException("ACTIVE 회원만 닉네임을 변경할 수 있습니다.");
        }
        this.nickname = requireNonNull(nickname);
    }

    public void suspend() {
        if (this.status != MemberStatus.ACTIVE) { // TODO : 커스텀예외
            throw new IllegalStateException("ACTIVE 상태에서만 정지할 수 있습니다.");
        }
        this.status = MemberStatus.SUSPENDED;
    }

    public void withdraw(Long requesterId) {
        requireNonNull(requesterId);
        if (!this.id.equals(requesterId)) { // TODO : 커스텀예외 필요. 권한 이슈
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
