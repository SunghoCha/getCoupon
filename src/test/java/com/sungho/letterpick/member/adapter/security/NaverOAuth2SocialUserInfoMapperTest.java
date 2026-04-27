package com.sungho.letterpick.member.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.auth.SocialUserInfo;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

class NaverOAuth2SocialUserInfoMapperTest {

    private final NaverOAuth2SocialUserInfoMapper mapper = new NaverOAuth2SocialUserInfoMapper();

    @Test
    @DisplayName("registrationId가 naver이면 supports는 true")
    void supports_returns_true_for_naver() {
        assertThat(mapper.supports("naver")).isTrue();
    }

    @Test
    @DisplayName("registrationId가 naver가 아니면 supports는 false")
    void supports_returns_false_for_other_provider() {
        assertThat(mapper.supports("google")).isFalse();
        assertThat(mapper.supports("")).isFalse();
    }

    @Test
    @DisplayName("response 맵의 모든 필드가 SocialUserInfo로 매핑된다")
    void converts_oauth2_user_to_social_user_info() {
        Map<String, Object> response = Map.of(
                "id", "naver-id-12345",
                "email", "test@naver.com",
                "nickname", "홍길동",
                "profile_image", "https://naver.com/pic.jpg"
        );
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("response", response));

        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        SocialUserInfo info = mapper.toSocialUserInfo(oAuth2User, request);

        assertThat(info.provider()).isEqualTo(SocialProvider.NAVER);
        assertThat(info.providerId()).isEqualTo("naver-id-12345");
        assertThat(info.email()).isEqualTo("test@naver.com");
        assertThat(info.nickname()).isEqualTo("홍길동");
        assertThat(info.profileImageUrl()).isEqualTo("https://naver.com/pic.jpg");
    }

    @Test
    @DisplayName("response 맵의 선택 필드(email/nickname/profile_image)가 null이면 SocialUserInfo에도 null로 전달된다")
    void allows_optional_fields_to_be_null() {
        Map<String, Object> response = new HashMap<>();
        response.put("id", "naver-id-12345");
        response.put("email", null);
        response.put("nickname", null);
        response.put("profile_image", null);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("response", response));

        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        SocialUserInfo info = mapper.toSocialUserInfo(oAuth2User, request);

        assertThat(info.providerId()).isEqualTo("naver-id-12345");
        assertThat(info.email()).isNull();
        assertThat(info.nickname()).isNull();
        assertThat(info.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("response 맵에 id가 없으면 IllegalArgumentException")
    void throws_when_id_is_missing() {
        Map<String, Object> response = new HashMap<>();
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("response", response));

        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        assertThatThrownBy(() -> mapper.toSocialUserInfo(oAuth2User, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    @Test
    @DisplayName("response 맵에 id가 blank이면 IllegalArgumentException")
    void throws_when_id_is_blank() {
        Map<String, Object> response = Map.of("id", "");
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("response", response));

        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        assertThatThrownBy(() -> mapper.toSocialUserInfo(oAuth2User, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    @Test
    @DisplayName("response 키 자체가 없으면 IllegalArgumentException")
    void throws_when_response_attribute_is_missing() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of());

        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        assertThatThrownBy(() -> mapper.toSocialUserInfo(oAuth2User, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Naver response");
    }

    @Test
    @DisplayName("response가 Map이 아니면 IllegalArgumentException")
    void throws_when_response_is_not_a_map() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("response", "not a map"));

        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        assertThatThrownBy(() -> mapper.toSocialUserInfo(oAuth2User, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Naver response");
    }
}
