package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.auth.SocialUserInfo;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GoogleOidcSocialUserInfoMapper implements OidcSocialUserInfoMapper {

    private static final SocialProvider PROVIDER = SocialProvider.GOOGLE;

    @Override
    public boolean supports(String registrationId) {
        return PROVIDER.registrationId().equals(registrationId);
    }

    @Override
    public SocialUserInfo toSocialUserInfo(OidcUser oidcUser, OidcUserRequest userRequest) {
        return new SocialUserInfo(
                PROVIDER,
                Objects.requireNonNull(oidcUser.getSubject(), "sub"),
                oidcUser.getEmail(),
                oidcUser.getFullName(),
                oidcUser.getPicture()
        );
    }
}
