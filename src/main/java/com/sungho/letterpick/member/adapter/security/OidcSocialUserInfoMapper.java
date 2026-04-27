package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialUserInfo;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcSocialUserInfoMapper {

     boolean supports(String registrationId);

     SocialUserInfo toSocialUserInfo(OidcUser oidcUser, OidcUserRequest userRequest);
}
