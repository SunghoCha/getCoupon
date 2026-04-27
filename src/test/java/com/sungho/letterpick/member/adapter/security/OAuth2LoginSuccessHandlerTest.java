package com.sungho.letterpick.member.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sungho.letterpick.common.auth.SocialPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class OAuth2LoginSuccessHandlerTest {

    private final OAuth2LoginSuccessHandler handler =
            new OAuth2LoginSuccessHandler("http://localhost:3000");

    @Test
    @DisplayName("가입 대기 principal은 회원 가입 페이지로 redirect된다")
    void redirects_to_signup_when_principal_is_pending() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SocialPrincipal principal = mock(SocialPrincipal.class);
        when(principal.isPending()).thenReturn(true);
        Authentication authentication = new TestingAuthenticationToken(principal, null);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/signup");
    }

    @Test
    @DisplayName("가입 완료 principal은 홈으로 redirect된다")
    void redirects_to_home_when_principal_is_existing_member() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SocialPrincipal principal = mock(SocialPrincipal.class);
        when(principal.isPending()).thenReturn(false);
        Authentication authentication = new TestingAuthenticationToken(principal, null);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/");
    }

    @Test
    @DisplayName("SocialPrincipal이 아닌 인증 성공 principal이면 실패한다")
    void throws_when_principal_is_not_social_principal() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new TestingAuthenticationToken("raw-principal", null);

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SocialPrincipal");
    }
}
