package com.sungho.letterpick.member.adapter.security;

import com.sungho.letterpick.common.auth.SocialPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String SIGNUP_PATH = "/signup";
    private static final String HOME_PATH = "/";

    private final String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(@Value("${frontend.base-url}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SocialPrincipal social)) {
            throw new IllegalStateException(
                    "OAuth2 login success principal은 SocialPrincipal이어야 합니다: "
                            + (principal == null ? "null" : principal.getClass().getName())
            );
        }

        String path = social.isPending() ? SIGNUP_PATH : HOME_PATH;
        response.sendRedirect(frontendBaseUrl + path);
    }
}
