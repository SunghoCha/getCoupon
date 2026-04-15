package com.sungho.letterpick.member.application.required;

import com.sungho.letterpick.member.domain.Email;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.Nickname;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(Email email);

    boolean existsByNickname(Nickname nickname);
}
