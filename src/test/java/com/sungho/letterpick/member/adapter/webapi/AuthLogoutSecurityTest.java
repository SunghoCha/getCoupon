package com.sungho.letterpick.member.adapter.webapi;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2UserService;
import com.sungho.letterpick.member.adapter.security.CustomOidcUserService;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginFailureHandler;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginSuccessHandler;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthLogoutSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MemberModifier memberModifier;

    @MockitoBean
    CustomOidcUserService customOidcUserService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @MockitoBean
    OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    @WithLoginUser
    @DisplayName("로그인된 회원이 /api/v1/auth/logout 호출 시 204")
    void logout_returns_204_for_authenticated_user() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비로그인 사용자가 /api/v1/auth/logout 호출 시 204 idempotent")
    void logout_returns_204_for_anonymous() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLoginUser
    @DisplayName("로그인된 회원이 CSRF 토큰 없이 /api/v1/auth/logout 호출 시 403")
    void logout_returns_403_without_csrf() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isForbidden());
    }
}
