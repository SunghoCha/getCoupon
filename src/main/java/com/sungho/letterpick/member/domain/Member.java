package com.sungho.letterpick.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.sungho.letterpick.member.domain.exception.MemberStatusException;

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
        ensureCanChangeNickname();
        this.nickname = requireNonNull(nickname);
    }

    public void suspend() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new MemberStatusException();
        }
        this.status = MemberStatus.SUSPENDED;
    }

    public void withdraw() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new MemberStatusException();
        }
        this.status = MemberStatus.DEACTIVATED;
    }

    public void withdrawByAdmin() {
        if (this.status != MemberStatus.SUSPENDED) {
            throw new MemberStatusException();
        }
        this.status = MemberStatus.DEACTIVATED;
    }

    public void ensureCanChangeNickname() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new MemberStatusException();
        }
    }
}
