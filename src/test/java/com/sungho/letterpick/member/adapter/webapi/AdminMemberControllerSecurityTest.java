package com.sungho.letterpick.member.adapter.webapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithAdminUser;
import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2UserService;
import com.sungho.letterpick.member.adapter.security.CustomOidcUserService;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginFailureHandler;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginSuccessHandler;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberSuspendRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AdminMemberController.class)
@Import(SecurityConfig.class)
class AdminMemberControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
    @WithLoginUser(memberId = 42L)
    @DisplayName("ROLE_ADMIN 없는 사용자가 /api/v1/admin/** 호출 시 403")
    void admin_path_returns_403_for_non_admin() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(100L);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        verify(memberModifier, never()).suspend(any());
    }

    @Test
    @WithAdminUser
    @DisplayName("ROLE_ADMIN 있는 사용자가 /api/v1/admin/** 호출 시 권한 통과")
    void admin_path_passes_for_admin() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(42L);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(memberModifier).suspend(request);
    }
}
