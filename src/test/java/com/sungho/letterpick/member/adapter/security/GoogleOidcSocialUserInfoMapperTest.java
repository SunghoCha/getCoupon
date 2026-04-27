package com.sungho.letterpick.member.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.auth.SocialUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleOidcSocialUserInfoMapperTest {

    private final GoogleOidcSocialUserInfoMapper mapper = new GoogleOidcSocialUserInfoMapper();

    @Test
    @DisplayName("registrationId가 google이면 supports는 true")
    void supports_returns_true_for_google() {
        assertThat(mapper.supports("google")).isTrue();
    }

    @Test
    @DisplayName("registrationId가 google이 아니면 supports는 false")
    void supports_returns_false_for_other_provider() {
        assertThat(mapper.supports("naver")).isFalse();
        assertThat(mapper.supports("kakao")).isFalse();
        assertThat(mapper.supports("")).isFalse();
    }

    @Test
    @DisplayName("OidcUser의 모든 필드가 SocialUserInfo로 매핑된다")
    void converts_oidc_user_to_social_user_info() {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn("123456789");
        when(oidcUser.getEmail()).thenReturn("test@gmail.com");
        when(oidcUser.getFullName()).thenReturn("홍길동");
        when(oidcUser.getPicture()).thenReturn("https://example.com/pic.jpg");

        OidcUserRequest request = mock(OidcUserRequest.class);
        SocialUserInfo info = mapper.toSocialUserInfo(oidcUser, request);

        assertThat(info.provider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(info.providerId()).isEqualTo("123456789");
        assertThat(info.email()).isEqualTo("test@gmail.com");
        assertThat(info.nickname()).isEqualTo("홍길동");
        assertThat(info.profileImageUrl()).isEqualTo("https://example.com/pic.jpg");
    }

    @Test
    @DisplayName("선택 필드(email/fullName/picture)가 null이면 SocialUserInfo에도 null로 전달된다")
    void allows_optional_fields_to_be_null() {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn("123456789");
        when(oidcUser.getEmail()).thenReturn(null);
        when(oidcUser.getFullName()).thenReturn(null);
        when(oidcUser.getPicture()).thenReturn(null);

        OidcUserRequest request = mock(OidcUserRequest.class);
        SocialUserInfo info = mapper.toSocialUserInfo(oidcUser, request);

        assertThat(info.providerId()).isEqualTo("123456789");
        assertThat(info.email()).isNull();
        assertThat(info.nickname()).isNull();
        assertThat(info.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("subject가 null이면 NullPointerException")
    void throws_when_subject_is_null() {
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn(null);

        OidcUserRequest request = mock(OidcUserRequest.class);
        assertThatThrownBy(() -> mapper.toSocialUserInfo(oidcUser, request))
                .isInstanceOf(NullPointerException.class);
    }
}
