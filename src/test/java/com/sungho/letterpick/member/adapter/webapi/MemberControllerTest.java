package com.sungho.letterpick.member.adapter.webapi;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithLoginUser;
import tools.jackson.databind.ObjectMapper;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.common.config.WebMvcConfig;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberNicknameChangeRequest;
import com.sungho.letterpick.member.domain.exception.DuplicateNicknameException;
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

@WebMvcTest(MemberController.class)
@Import({WebMvcConfig.class, SecurityConfig.class})
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberModifier memberModifier;

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("DELETE /api/v1/members/me 요청 시 204가 반환되고 서비스에 탈퇴가 위임된다")
    void withdraw_returns_204_and_delegates_to_service() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me"))
                .andExpect(status().isNoContent());

        verify(memberModifier).withdraw(42L);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("탈퇴 시 회원을 찾지 못하면 404가 반환된다")
    void withdraw_returns_404_when_member_not_found() throws Exception {
        doThrow(new MemberNotFoundException()).when(memberModifier).withdraw(42L);

        mockMvc.perform(delete("/api/v1/members/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEM-003"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("탈퇴 시 허용되지 않는 상태이면 409가 반환된다")
    void withdraw_returns_409_when_status_violation() throws Exception {
        doThrow(new MemberStatusException()).when(memberModifier).withdraw(42L);

        mockMvc.perform(delete("/api/v1/members/me"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-004"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("PATCH /api/v1/members/me 요청 시 204가 반환되고 서비스에 닉네임 변경이 위임된다")
    void changeNickname_returns_204_and_delegates_to_service() throws Exception {
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest("새닉네임");

        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(memberModifier).changeNickname(42L, request);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("닉네임 변경 시 회원을 찾지 못하면 404가 반환된다")
    void changeNickname_returns_404_when_member_not_found() throws Exception {
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest("새닉네임");
        doThrow(new MemberNotFoundException())
                .when(memberModifier).changeNickname(42L, request);

        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEM-003"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("닉네임 변경 시 닉네임이 중복되면 409가 반환된다")
    void changeNickname_returns_409_when_nickname_duplicated() throws Exception {
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest("중복닉네임");
        doThrow(new DuplicateNicknameException())
                .when(memberModifier).changeNickname(42L, request);

        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-002"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("닉네임 변경 시 허용되지 않는 상태이면 409가 반환된다")
    void changeNickname_returns_409_when_status_violation() throws Exception {
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest("새닉네임");
        doThrow(new MemberStatusException())
                .when(memberModifier).changeNickname(42L, request);

        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-004"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("닉네임 변경 시 blank 입력이면 400이 반환된다")
    void changeNickname_returns_400_when_nickname_blank() throws Exception {
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest("");

        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
