package com.sungho.letterpick.member.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

class OAuth2LoginFailureHandlerTest {

    private final OAuth2LoginFailureHandler handler =
            new OAuth2LoginFailureHandler("http://localhost:3000");

    @Test
    @DisplayName("허용된 OAuth2 실패 코드는 로그인 페이지 redirect query로 전달된다")
    void redirects_with_allowed_oauth2_error_code() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("login_refused", "로그인할 수 없는 회원 상태입니다.", null)
        );

        handler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?error=login_refused");
    }

    @Test
    @DisplayName("email_required 코드도 redirect query로 전달된다")
    void redirects_with_email_required_error_code() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("email_required", "소셜 계정에서 이메일을 제공하지 않아 가입할 수 없습니다.", null)
        );

        handler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?error=email_required");
    }

    @Test
    @DisplayName("알 수 없는 인증 실패는 generic 실패 코드로 redirect된다")
    void redirects_with_generic_error_code_for_unknown_exception() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("raw message"));

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?error=oauth2_login_failed");
    }
}
