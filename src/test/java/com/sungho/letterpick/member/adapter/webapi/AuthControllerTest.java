package com.sungho.letterpick.member.adapter.webapi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.common.auth.WithPendingSocialUser;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberRegisterRequest;
import com.sungho.letterpick.member.application.provided.MemberSignupRequest;
import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberModifier memberModifier;

    @Test
    @WithPendingSocialUser
    @DisplayName("회원 가입 중 이메일 unique constraint 충돌 시 409")
    void signup_returns_409_when_email_unique_constraint_violated() throws Exception {
        when(memberModifier.register(any(MemberRegisterRequest.class)))
                .thenThrow(dataIntegrityViolation("uk_member_email"));

        MemberSignupRequest request = new MemberSignupRequest("새닉네임");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-001"));
    }

    @Test
    @WithPendingSocialUser
    @DisplayName("회원 가입 중 소셜 계정 unique constraint 충돌 시 409")
    void signup_returns_409_when_social_identity_unique_constraint_violated() throws Exception {
        when(memberModifier.register(any(MemberRegisterRequest.class)))
                .thenThrow(dataIntegrityViolation("uk_member_social_identity"));

        MemberSignupRequest request = new MemberSignupRequest("새닉네임");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEM-006"));
    }

    private DataIntegrityViolationException dataIntegrityViolation(String constraintName) {
        return new DataIntegrityViolationException(
                "Unique constraint violation",
                new ConstraintViolationException(
                        "Unique constraint violation",
                        new SQLException("constraint violation"),
                        constraintName
                )
        );
    }
}
