package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialUserInfo;
import com.sungho.letterpick.member.application.provided.SocialLoginService;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.SocialIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
    OIDC 로그인 토큰 교환이 끝난 뒤,
    주어진 OIDC 사용자 요청 정보를 바탕으로
    최종 인증 주체인 OidcUser를 만들어 반환
 */

@Component
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final SocialUserInfoFactory socialUserInfoFactory;
    private final SocialLoginService socialLoginService;


    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        SocialUserInfo info = socialUserInfoFactory.createFromOidc(oidcUser, userRequest);

        SocialIdentity identity = new SocialIdentity(info.provider(), info.providerId());
        Optional<Member> existingMember = socialLoginService.findExistingMember(identity);

        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            if (!socialLoginService.canLogin(member)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("login_refused", "로그인할 수 없는 회원 상태입니다.", null)
                );
            }
            return CustomOidcPrincipal.existing(member, oidcUser);
        }

        if (info.email() == null || info.email().isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_required", "소셜 계정에서 이메일을 제공하지 않아 가입할 수 없습니다.", null)
            );
        }

        return CustomOidcPrincipal.pending(info, oidcUser);
    }
}
