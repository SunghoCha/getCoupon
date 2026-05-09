package com.sungho.letterpick.member.adapter.webapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.auth.WithPendingSocialUser;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2UserService;
import com.sungho.letterpick.member.adapter.security.CustomOidcUserService;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginFailureHandler;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginSuccessHandler;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberRegisterRequest;
import com.sungho.letterpick.member.application.provided.MemberSignupRequest;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerSecurityTest {

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
    @DisplayName("익명 사용자가 /api/v1/auth/signup 호출 시 401")
    void signup_returns_401_for_anonymous() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("새닉네임");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(memberModifier, never()).register(any(MemberRegisterRequest.class));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("ROLE_PENDING_SIGNUP 없는 사용자가 /api/v1/auth/signup 호출 시 403")
    void signup_returns_403_for_user_without_pending_signup() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("새닉네임");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(memberModifier, never()).register(any(MemberRegisterRequest.class));
    }

    @Test
    @WithPendingSocialUser
    @DisplayName("ROLE_PENDING_SIGNUP 사용자가 CSRF 토큰 없이 /api/v1/auth/signup 호출 시 403")
    void signup_returns_403_for_pending_user_without_csrf() throws Exception {
        MemberSignupRequest request = new MemberSignupRequest("새닉네임");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(memberModifier, never()).register(any(MemberRegisterRequest.class));
    }

    @Test
    @WithPendingSocialUser
    @DisplayName("ROLE_PENDING_SIGNUP 사용자가 /api/v1/auth/signup 호출 시 가입 완료")
    void signup_returns_201_for_pending_user() throws Exception {
        Member savedMember = MemberFixture.createMemberWithId(99L);
        when(memberModifier.register(any(MemberRegisterRequest.class))).thenReturn(savedMember);

        MemberSignupRequest request = new MemberSignupRequest("새닉네임");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(memberModifier).register(any(MemberRegisterRequest.class));
    }

}
