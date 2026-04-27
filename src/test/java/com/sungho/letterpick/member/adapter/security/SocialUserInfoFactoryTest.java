package com.sungho.letterpick.member.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

class SocialUserInfoFactoryTest {

    private final SocialUserInfoFactory factory = new SocialUserInfoFactory(List.of(), List.of());

    @Test
    @DisplayName("지원하지 않는 OAuth2 provider이면 인증 예외로 실패 원인을 드러낸다")
    void createFromOAuth2_throws_authentication_exception_for_unsupported_provider() {
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        when(request.getClientRegistration()).thenReturn(clientRegistration("kakao"));

        assertThatThrownBy(() -> factory.createFromOAuth2(mock(OAuth2User.class), request))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception -> {
                    assertThat(exception.getError().getErrorCode()).isEqualTo("unsupported_oauth2_provider");
                    assertThat(exception.getError().getDescription()).contains("kakao");
                });
    }

    @Test
    @DisplayName("지원하지 않는 OIDC provider이면 인증 예외로 실패 원인을 드러낸다")
    void createFromOidc_throws_authentication_exception_for_unsupported_provider() {
        OidcUserRequest request = mock(OidcUserRequest.class);
        when(request.getClientRegistration()).thenReturn(clientRegistration("kakao"));

        assertThatThrownBy(() -> factory.createFromOidc(mock(OidcUser.class), request))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception -> {
                    assertThat(exception.getError().getErrorCode()).isEqualTo("unsupported_oauth2_provider");
                    assertThat(exception.getError().getDescription()).contains("kakao");
                });
    }

    private ClientRegistration clientRegistration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://example.com/oauth/authorize")
                .tokenUri("https://example.com/oauth/token")
                .userInfoUri("https://example.com/user")
                .userNameAttributeName("id")
                .clientName(registrationId)
                .build();
    }
}
