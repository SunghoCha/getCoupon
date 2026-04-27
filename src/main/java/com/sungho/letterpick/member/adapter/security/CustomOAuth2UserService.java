package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialUserInfo;
import com.sungho.letterpick.member.application.provided.SocialLoginService;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.SocialIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
    일반 OAuth2 로그인(Naver 등) 토큰 교환 후
    UserInfo Endpoint 응답을 우리 도메인 모델로 변환하고
    최종 인증 주체인 OAuth2User를 만들어 반환
 */

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final SocialUserInfoFactory socialUserInfoFactory;
    private final SocialLoginService socialLoginService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        SocialUserInfo info = socialUserInfoFactory.createFromOAuth2(oAuth2User, userRequest);

        SocialIdentity identity = new SocialIdentity(info.provider(), info.providerId());
        Optional<Member> existingMember = socialLoginService.findExistingMember(identity);

        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            if (!socialLoginService.canLogin(member)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("login_refused", "로그인할 수 없는 회원 상태입니다.", null)
                );
            }
            return CustomOAuth2Principal.existing(member, oAuth2User);
        }

        if (info.email() == null || info.email().isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_required", "소셜 계정에서 이메일을 제공하지 않아 가입할 수 없습니다.", null)
            );
        }

        return CustomOAuth2Principal.pending(info, oAuth2User);
    }
}
