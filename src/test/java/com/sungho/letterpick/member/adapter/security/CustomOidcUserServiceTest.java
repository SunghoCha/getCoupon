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
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

class CustomOidcUserServiceTest {

    private final SocialUserInfoFactory socialUserInfoFactory = mock(SocialUserInfoFactory.class);
    private final SocialLoginService socialLoginService = mock(SocialLoginService.class);
    private final OidcUserService delegate = mock(OidcUserService.class);

    private final CustomOidcUserService userService =
            new CustomOidcUserService(socialUserInfoFactory, socialLoginService);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "delegate", delegate);
    }

    @Test
    @DisplayName("기존 회원이고 로그인 가능 상태이면 existing principal을 반환한다")
    void returns_existing_principal_when_member_exists_and_can_login() {
        OidcUserRequest userRequest = mock(OidcUserRequest.class);
        OidcUser oidcUser = mock(OidcUser.class);
        SocialUserInfo info = googleInfo("user@gmail.com");
        Member member = MemberFixture.createMemberWithId(42L);

        when(delegate.loadUser(userRequest)).thenReturn(oidcUser);
        when(socialUserInfoFactory.createFromOidc(oidcUser, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.of(member));
        when(socialLoginService.canLogin(member)).thenReturn(true);

        OidcUser result = userService.loadUser(userRequest);

        assertThat(result).isInstanceOf(SocialPrincipal.class);
        SocialPrincipal principal = (SocialPrincipal) result;
        assertThat(principal.isPending()).isFalse();
        assertThat(principal.getMember().getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("기존 회원이지만 로그인 불가 상태이면 login_refused로 실패한다")
    void throws_login_refused_when_member_cannot_login() {
        OidcUserRequest userRequest = mock(OidcUserRequest.class);
        OidcUser oidcUser = mock(OidcUser.class);
        SocialUserInfo info = googleInfo("user@gmail.com");
        Member member = MemberFixture.createMemberWithId(42L);

        when(delegate.loadUser(userRequest)).thenReturn(oidcUser);
        when(socialUserInfoFactory.createFromOidc(oidcUser, userRequest)).thenReturn(info);
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
        OidcUserRequest userRequest = mock(OidcUserRequest.class);
        OidcUser oidcUser = mock(OidcUser.class);
        SocialUserInfo info = googleInfo("user@gmail.com");

        when(delegate.loadUser(userRequest)).thenReturn(oidcUser);
        when(socialUserInfoFactory.createFromOidc(oidcUser, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.empty());

        OidcUser result = userService.loadUser(userRequest);

        assertThat(result).isInstanceOf(SocialPrincipal.class);
        SocialPrincipal principal = (SocialPrincipal) result;
        assertThat(principal.isPending()).isTrue();
        assertThat(principal.getSocialUserInfo()).isEqualTo(info);
    }

    @Test
    @DisplayName("신규 회원인데 IDP가 이메일을 제공하지 않으면 email_required로 실패한다")
    void throws_email_required_when_new_user_without_email() {
        OidcUserRequest userRequest = mock(OidcUserRequest.class);
        OidcUser oidcUser = mock(OidcUser.class);
        SocialUserInfo info = googleInfo(null);

        when(delegate.loadUser(userRequest)).thenReturn(oidcUser);
        when(socialUserInfoFactory.createFromOidc(oidcUser, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUser(userRequest))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode()).isEqualTo("email_required"));
    }

    @Test
    @DisplayName("신규 회원인데 이메일이 blank이면 email_required로 실패한다")
    void throws_email_required_when_new_user_with_blank_email() {
        OidcUserRequest userRequest = mock(OidcUserRequest.class);
        OidcUser oidcUser = mock(OidcUser.class);
        SocialUserInfo info = googleInfo("   ");

        when(delegate.loadUser(userRequest)).thenReturn(oidcUser);
        when(socialUserInfoFactory.createFromOidc(oidcUser, userRequest)).thenReturn(info);
        when(socialLoginService.findExistingMember(any(SocialIdentity.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUser(userRequest))
                .isInstanceOfSatisfying(OAuth2AuthenticationException.class, exception ->
                        assertThat(exception.getError().getErrorCode()).isEqualTo("email_required"));
    }

    private SocialUserInfo googleInfo(String email) {
        return new SocialUserInfo(SocialProvider.GOOGLE, "google-sub-12345", email, "테스터", null);
    }
}
