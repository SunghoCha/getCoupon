package com.sungho.letterpick.member.adapter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final String LOGIN_PATH = "/login";
    private static final String DEFAULT_ERROR_CODE = "oauth2_login_failed";
    private static final Set<String> ALLOWED_ERROR_CODES = Set.of(
            "login_refused",
            "unsupported_oauth2_provider",
            "email_required"
    );

    private final String frontendBaseUrl;

    public OAuth2LoginFailureHandler(@Value("${frontend.base-url}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String errorCode = resolveErrorCode(exception);
        log.info("OAuth2 login failed: code={}, exception={}",
                errorCode,
                exception.getClass().getSimpleName());

        String encodedErrorCode = URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
        response.sendRedirect(frontendBaseUrl + LOGIN_PATH + "?error=" + encodedErrorCode);
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();
            if (ALLOWED_ERROR_CODES.contains(errorCode)) {
                return errorCode;
            }
        }
        return DEFAULT_ERROR_CODE;
    }
}
