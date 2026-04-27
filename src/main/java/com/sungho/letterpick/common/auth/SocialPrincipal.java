package com.sungho.letterpick.common.auth;

import com.sungho.letterpick.member.domain.Member;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface SocialPrincipal extends OAuth2User {

    boolean isPending();

    Member getMember();

    SocialUserInfo getSocialUserInfo();

    SocialPrincipal withMember(Member member);
}
