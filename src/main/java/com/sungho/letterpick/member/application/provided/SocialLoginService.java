package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.SocialIdentity;

import java.util.Optional;

public interface SocialLoginService {

    Optional<Member> findExistingMember(SocialIdentity identity);

    boolean canLogin(Member member);
}
