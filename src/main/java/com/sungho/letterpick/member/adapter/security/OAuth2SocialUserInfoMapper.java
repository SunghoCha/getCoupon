package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialUserInfo;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2SocialUserInfoMapper {

    boolean supports(String registrationId);

    SocialUserInfo toSocialUserInfo(OAuth2User oAuth2User, OAuth2UserRequest userRequest);
}
