package com.sungho.letterpick.member.adapter.webapi;

import static com.sungho.letterpick.common.auth.SecurityAuthorities.ROLE_ADMIN;
import static com.sungho.letterpick.common.auth.SecurityAuthorities.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sungho.letterpick.LetterPickTestConfiguration;
import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2Principal;
import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.application.provided.MemberNicknameChangeRequest;
import com.sungho.letterpick.member.application.provided.MemberSuspendRequest;
import com.sungho.letterpick.member.application.provided.MemberWithdrawByAdminRequest;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberFixture;
import com.sungho.letterpick.member.domain.MemberStatus;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(LetterPickTestConfiguration.class)
class MemberControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void cleanUp() {
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("회원이 닉네임을 변경하면 DB에 반영된다")
    void changeNickname_flow() throws Exception {
        Member saved = memberRepository.save(MemberFixture.createMember());
        MemberNicknameChangeRequest request = new MemberNicknameChangeRequest("새닉네임");

        mockMvc.perform(patch("/api/v1/members/me")
                        .with(authentication(selfAuth(saved)))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getNickname().name()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("회원이 탈퇴하면 상태가 DEACTIVATED로 전이된다")
    void withdraw_flow() throws Exception {
        Member saved = memberRepository.save(MemberFixture.createMember());

        mockMvc.perform(delete("/api/v1/members/me")
                        .with(authentication(selfAuth(saved)))
                        .with(csrf().asHeader()))
                .andExpect(status().isNoContent());

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
    }

    @Test
    @DisplayName("관리자가 회원을 정지하면 상태가 SUSPENDED로 전이된다")
    void suspend_flow() throws Exception {
        Member saved = memberRepository.save(MemberFixture.createMember());
        MemberSuspendRequest request = new MemberSuspendRequest(saved.getId());

        mockMvc.perform(post("/api/v1/admin/members/suspension")
                        .with(authentication(adminAuth()))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
    }

    @Test
    @DisplayName("관리자가 정지된 회원을 탈퇴 처리하면 상태가 DEACTIVATED로 전이된다")
    void withdrawByAdmin_flow() throws Exception {
        Member member = MemberFixture.createMember();
        member.suspend();
        Member saved = memberRepository.save(member);
        MemberWithdrawByAdminRequest request = new MemberWithdrawByAdminRequest(saved.getId());

        mockMvc.perform(post("/api/v1/admin/members/withdrawal")
                        .with(authentication(adminAuth()))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Member found = memberRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
    }

    @Test
    @DisplayName("OpenAPI 스펙에 회원 도메인 4개 엔드포인트가 문서화된다")
    void openapi_spec_includes_member_endpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/members/me'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/members/me'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/members/suspension'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/members/withdrawal'].post").exists());
    }

    private Authentication selfAuth(Member member) {
        OAuth2User delegate = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(ROLE_USER)),
                Map.of("sub", "test-sub-" + member.getId()),
                "sub"
        );
        CustomOAuth2Principal principal = CustomOAuth2Principal.existing(member, delegate);
        return new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "test-registration"
        );
    }

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                new LoginUser(999L),
                null,
                List.of(new SimpleGrantedAuthority(ROLE_ADMIN))
        );
    }
}
