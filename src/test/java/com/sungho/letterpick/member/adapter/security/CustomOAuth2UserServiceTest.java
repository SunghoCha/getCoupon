package com.sungho.letterpick.member.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sungho.letterpick.common.auth.SocialPrincipal;
import com.sungho.letterpick.common.auth.SocialProvider;
import com.sungho.letterpick.common.auth.SocialUserInfo;
import com.sungho.letterpick.member.application.provided.SocialLoginService;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberFixture;
import com.sungho.letterpick.member.domain.SocialIdentity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

class CustomOAuth2UserServiceTest {

    private final SocialUserInfoFactory socialUserInfoFactory = mock(SocialUserInfoFactory.class);
    private final SocialLoginService socialLoginService = mock(SocialLoginService.class);
    private final DefaultOAuth2UserService delegate = mock(DefaultOAuth2UserService.class);

    private final CustomOAuth2UserService userService =
            new CustomOAuth2UserService(socialUserInfoFactory, socialLoginService);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "delegate", delegate);
    }

    @Test
    @DisplayName("기존 회원이고 로그인 가능 상태이면 existing principal을 반환한다")
    void returns_existing_principal_when_member_exists_and_can_login() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        SocialUserInfo info = naverInfo("user@naver.com");
        Member member = MemberFixture.createMemberWithId(42L);

        when(delegate.loadUser(userRequest)).thenReturn(oAuth2User);
        when(socialUserInfoFactory.createFromOAuth2(oAuth2User, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.of(member));
        when(socialLoginService.canLogin(member)).thenReturn(true);

        OAuth2User result = userService.loadUser(userRequest);

        assertThat(result).isInstanceOf(SocialPrincipal.class);
        SocialPrincipal principal = (SocialPrincipal) result;
        assertThat(principal.isPending()).isFalse();
        assertThat(principal.getMember().getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("기존 회원이지만 로그인 불가 상태이면 login_refused로 실패한다")
    void throws_login_refused_when_member_cannot_login() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        SocialUserInfo info = naverInfo("user@naver.com");
        Member member = MemberFixture.createMemberWithId(42L);

        when(delegate.loadUser(userRequest)).thenReturn(oAuth2User);
        when(socialUserInfoFactory.createFromOAuth2(oAuth2User, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.of(member));
        when(socialLoginService.canLogin(member)).thenReturn(false);

        assertThatThrownBy(() -> userService.loadUser(userRequest))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode()).isEqualTo("login_refused"));
    }

    @Test
    @DisplayName("신규 회원이고 IDP가 이메일을 제공하면 pending principal을 반환한다")
    void returns_pending_principal_when_new_user_with_email() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        SocialUserInfo info = naverInfo("user@naver.com");

        when(delegate.loadUser(userRequest)).thenReturn(oAuth2User);
        when(socialUserInfoFactory.createFromOAuth2(oAuth2User, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.empty());

        OAuth2User result = userService.loadUser(userRequest);

        assertThat(result).isInstanceOf(SocialPrincipal.class);
        SocialPrincipal principal = (SocialPrincipal) result;
        assertThat(principal.isPending()).isTrue();
        assertThat(principal.getSocialUserInfo()).isEqualTo(info);
    }

    @Test
    @DisplayName("신규 회원인데 IDP가 이메일을 제공하지 않으면 email_required로 실패한다")
    void throws_email_required_when_new_user_without_email() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        SocialUserInfo info = naverInfo(null);

        when(delegate.loadUser(userRequest)).thenReturn(oAuth2User);
        when(socialUserInfoFactory.createFromOAuth2(oAuth2User, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUser(userRequest))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode()).isEqualTo("email_required"));
    }

    @Test
    @DisplayName("신규 회원인데 이메일이 blank이면 email_required로 실패한다")
    void throws_email_required_when_new_user_with_blank_email() {
        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        SocialUserInfo info = naverInfo("   ");

        when(delegate.loadUser(userRequest)).thenReturn(oAuth2User);
        when(socialUserInfoFactory.createFromOAuth2(oAuth2User, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUser(userRequest))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode()).isEqualTo("email_required"));
    }

    private SocialUserInfo naverInfo(String email) {
        return new SocialUserInfo(SocialProvider.NAVER, "naver-id-12345", email, "테스터", null);
    }
}
