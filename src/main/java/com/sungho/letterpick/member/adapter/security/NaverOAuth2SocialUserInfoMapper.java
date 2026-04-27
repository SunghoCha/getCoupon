package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.auth.SocialUserInfo;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NaverOAuth2SocialUserInfoMapper implements OAuth2SocialUserInfoMapper {

    private static final SocialProvider PROVIDER = SocialProvider.NAVER;
    private static final String RESPONSE = "response";

    @Override
    public boolean supports(String registrationId) {
        return PROVIDER.registrationId().equals(registrationId);
    }

    @Override
    public SocialUserInfo toSocialUserInfo(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        Map<String, Object> response = getResponse(oAuth2User);

        return new SocialUserInfo(
                PROVIDER,
                requireAttribute(response, "id"),
                getAttribute(response, "email"),
                getAttribute(response, "nickname"),
                getAttribute(response, "profile_image")
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse(OAuth2User oAuth2User) {
        Object response = oAuth2User.getAttributes().get(RESPONSE);
        if (!(response instanceof Map)) { // TODO : 커스텀 예외?
            throw new IllegalArgumentException("Missing or invalid Naver response attributes");
        }
        return (Map<String, Object>) response;
    }

    private String getAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value != null ? String.valueOf(value) : null;
    }

    private String requireAttribute(Map<String, Object> attributes, String name) {
        String value = getAttribute(attributes, name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required Naver attribute: " + name);
        }
        return value;
    }
}
