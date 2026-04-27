package com.sungho.letterpick.member.adapter.webapi;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.common.config.WebMvcConfig;
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

@WebMvcTest(MemberController.class)
@Import({SecurityConfig.class, WebMvcConfig.class})
class MemberControllerSecurityTest {

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
    @DisplayName("익명 사용자가 /api/v1/members/** 호출 시 401")
    void members_path_returns_401_for_anonymous() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("인증 사용자가 /api/v1/members/** 호출 시 권한 통과")
    void members_path_passes_for_authenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(memberModifier).withdraw(42L);
    }
}
