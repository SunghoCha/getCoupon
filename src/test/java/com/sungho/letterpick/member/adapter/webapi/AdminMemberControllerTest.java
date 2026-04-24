package com.sungho.letterpick.member.adapter.webapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberSuspendRequest;
import com.sungho.letterpick.member.application.provided.MemberWithdrawByAdminRequest;
import com.sungho.letterpick.member.domain.exception.MemberNotFoundException;
import com.sungho.letterpick.member.domain.exception.MemberStatusException;
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
class AdminMemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberModifier memberModifier;

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("관리자가 회원 정지 요청 시 204가 반환되고 서비스에 위임된다")
    void suspend_returns_204_when_admin() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(42L);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(memberModifier).suspend(request);
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("정지 대상 회원이 없으면 404가 반환된다")
    void suspend_returns_404_when_member_not_found() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(42L);
        doThrow(new MemberNotFoundException())
                .when(memberModifier).suspend(request);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEM-003"));
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("정지 대상이 ACTIVE가 아니면 409가 반환된다")
    void suspend_returns_409_when_status_violation() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(42L);
        doThrow(new MemberStatusException())
                .when(memberModifier).suspend(request);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-004"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("일반 회원이 정지 API를 호출하면 403이 반환된다")
    void suspend_returns_403_when_not_admin() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(100L);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        verify(memberModifier, never()).suspend(any());
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("memberId가 null이면 400이 반환된다")
    void suspend_returns_400_when_memberId_null() throws Exception {
        MemberSuspendRequest request = new MemberSuspendRequest(null);

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verify(memberModifier, never()).suspend(any());
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("관리자가 회원 탈퇴 처리 요청 시 204가 반환되고 서비스에 위임된다")
    void withdrawByAdmin_returns_204_when_admin() throws Exception {
        MemberWithdrawByAdminRequest request = new MemberWithdrawByAdminRequest(42L);

        mockMvc.perform(post("/api/v1/admin/members/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(memberModifier).withdrawByAdmin(request);
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("탈퇴 처리 대상 회원이 없으면 404가 반환된다")
    void withdrawByAdmin_returns_404_when_member_not_found() throws Exception {
        MemberWithdrawByAdminRequest request = new MemberWithdrawByAdminRequest(42L);
        doThrow(new MemberNotFoundException())
                .when(memberModifier).withdrawByAdmin(request);

        mockMvc.perform(post("/api/v1/admin/members/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEM-003"));
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("탈퇴 처리 대상이 SUSPENDED가 아니면 409가 반환된다")
    void withdrawByAdmin_returns_409_when_status_violation() throws Exception {
        MemberWithdrawByAdminRequest request = new MemberWithdrawByAdminRequest(42L);
        doThrow(new MemberStatusException())
                .when(memberModifier).withdrawByAdmin(request);

        mockMvc.perform(post("/api/v1/admin/members/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-004"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("일반 회원이 탈퇴 처리 API를 호출하면 403이 반환된다")
    void withdrawByAdmin_returns_403_when_not_admin() throws Exception {
        MemberWithdrawByAdminRequest request = new MemberWithdrawByAdminRequest(100L);

        mockMvc.perform(post("/api/v1/admin/members/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        verify(memberModifier, never()).withdrawByAdmin(any());
    }

    @Test
    @WithLoginUser(memberId = 1L, authorities = {"ROLE_ADMIN"})
    @DisplayName("탈퇴 처리 요청에서 memberId가 null이면 400이 반환된다")
    void withdrawByAdmin_returns_400_when_memberId_null() throws Exception {
        MemberWithdrawByAdminRequest request = new MemberWithdrawByAdminRequest(null);

        mockMvc.perform(post("/api/v1/admin/members/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        verify(memberModifier, never()).withdrawByAdmin(any());
    }
}
