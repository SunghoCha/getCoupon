package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.common.config.WebMvcConfig;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2UserService;
import com.sungho.letterpick.member.adapter.security.CustomOidcUserService;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginFailureHandler;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginSuccessHandler;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueDetail;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueFinder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static com.sungho.letterpick.common.auth.SecurityAuthorities.ROLE_PENDING_SIGNUP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsletterIssueController.class)
@Import({SecurityConfig.class, WebMvcConfig.class})
class NewsletterIssueControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NewsletterIssueFinder newsletterIssueFinder;

    @MockitoBean
    CustomOidcUserService customOidcUserService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @MockitoBean
    OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    @DisplayName("익명 사용자가 오늘 도착한 뉴스레터 이슈 목록 조회 시 401")
    void getTodayIssues_returns_401_for_anonymous() throws Exception {
        mockMvc.perform(get("/api/v1/me/newsletter-issues/today"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithLoginUser(memberId = 42L, authorities = {ROLE_PENDING_SIGNUP})
    @DisplayName("가입 대기 사용자가 오늘 도착한 뉴스레터 이슈 목록 조회 시 403")
    void getTodayIssues_returns_403_for_pending_signup_user() throws Exception {
        mockMvc.perform(get("/api/v1/me/newsletter-issues/today"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(newsletterIssueFinder);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("인증 사용자가 오늘 도착한 뉴스레터 이슈 목록 조회 시 권한 통과")
    void getTodayIssues_passes_for_authenticated() throws Exception {
        given(newsletterIssueFinder.findTodayIssues(eq(42L), any(Pageable.class)))
                .willReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 20), false));

        mockMvc.perform(get("/api/v1/me/newsletter-issues/today"))
                .andExpect(status().isOk());

        verify(newsletterIssueFinder).findTodayIssues(eq(42L), any(Pageable.class));
    }

    @Test
    @DisplayName("익명 사용자가 뉴스레터 이슈 상세 조회 시 401")
    void getIssueDetail_returns_401_for_anonymous() throws Exception {
        mockMvc.perform(get("/api/v1/me/newsletter-issues/{issueId}", 10L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(newsletterIssueFinder);
    }

    @Test
    @WithLoginUser(memberId = 42L, authorities = {ROLE_PENDING_SIGNUP})
    @DisplayName("가입 대기 사용자가 뉴스레터 이슈 상세 조회 시 403")
    void getIssueDetail_returns_403_for_pending_signup_user() throws Exception {
        mockMvc.perform(get("/api/v1/me/newsletter-issues/{issueId}", 10L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(newsletterIssueFinder);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("인증 사용자가 뉴스레터 이슈 상세 조회 시 권한 통과")
    void getIssueDetail_passes_for_authenticated() throws Exception {
        NewsletterIssueDetail detail = new NewsletterIssueDetail(
                10L,
                1L,
                "Example Letter",
                "https://example.com/image.png",
                "뉴스레터 제목",
                "<p>뉴스레터 본문</p>",
                Instant.parse("2050-05-12T01:00:00Z"),
                true
        );
        given(newsletterIssueFinder.readIssueDetail(42L, 10L))
                .willReturn(detail);

        mockMvc.perform(get("/api/v1/me/newsletter-issues/{issueId}", 10L))
                .andExpect(status().isOk());

        verify(newsletterIssueFinder).readIssueDetail(42L, 10L);
    }
}
