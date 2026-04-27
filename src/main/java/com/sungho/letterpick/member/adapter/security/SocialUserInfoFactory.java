package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialUserInfoFactory {

    private final List<OidcSocialUserInfoMapper> oidcMappers;
    private final List<OAuth2SocialUserInfoMapper> oauth2Mappers;

    public SocialUserInfo createFromOidc(OidcUser oidcUser,
                                         OidcUserRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();

        return oidcMappers.stream()
                .filter(mapper -> mapper.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> unsupportedProvider(registrationId))
                .toSocialUserInfo(oidcUser, request);
    }

    public SocialUserInfo createFromOAuth2(OAuth2User oAuth2User,
                                           OAuth2UserRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();

        return oauth2Mappers.stream()
                .filter(mapper -> mapper.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> unsupportedProvider(registrationId))
                .toSocialUserInfo(oAuth2User, request);
    }

    private OAuth2AuthenticationException unsupportedProvider(String registrationId) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(
                        "unsupported_oauth2_provider",
                        "지원하지 않는 OAuth2 provider: " + registrationId,
                        null
                )
        );
    }
}
