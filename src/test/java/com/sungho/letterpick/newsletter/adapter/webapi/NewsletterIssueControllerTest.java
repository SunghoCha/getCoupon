package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.WebMvcConfig;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsletterIssueController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcConfig.class)
class NewsletterIssueControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NewsletterIssueFinder newsletterIssueFinder;

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("오늘 도착한 뉴스레터 이슈 조회 요청 시 200과 이슈 목록 페이지 응답이 반환된다")
    void getTodayIssues_returns_200_and_newsletter_issue_page_response() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        List<NewsletterIssueItem> issues = List.of(
                new NewsletterIssueItem(
                        10L,
                        1L,
                        "Example Letter",
                        "https://example.com/image.png",
                        "오늘의 뉴스레터",
                        "본문 미리보기",
                        Instant.parse("2050-05-12T01:00:00Z"),
                        false
                )
        );
        given(newsletterIssueFinder.findTodayIssues(eq(42L), any(Pageable.class)))
                .willReturn(new SliceImpl<>(issues, pageable, true));

        // when & then
        mockMvc.perform(get("/api/v1/me/newsletter-issues/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].issueId").value(10L))
                .andExpect(jsonPath("$.items[0].newsletterId").value(1L))
                .andExpect(jsonPath("$.items[0].newsletterName").value("Example Letter"))
                .andExpect(jsonPath("$.items[0].newsletterImageUrl").value("https://example.com/image.png"))
                .andExpect(jsonPath("$.items[0].subject").value("오늘의 뉴스레터"))
                .andExpect(jsonPath("$.items[0].previewText").value("본문 미리보기"))
                .andExpect(jsonPath("$.items[0].receivedAt").value("2050-05-12T01:00:00Z"))
                .andExpect(jsonPath("$.items[0].read").value(false))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.hasNext").value(true));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(newsletterIssueFinder).findTodayIssues(eq(42L), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("오늘 도착한 뉴스레터 이슈가 없으면 빈 목록 페이지 응답이 반환된다")
    void getTodayIssues_returns_empty_items_when_today_issues_do_not_exist() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        given(newsletterIssueFinder.findTodayIssues(eq(42L), any(Pageable.class)))
                .willReturn(new SliceImpl<>(List.of(), pageable, false));

        // when & then
        mockMvc.perform(get("/api/v1/me/newsletter-issues/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.hasNext").value(false));

        verify(newsletterIssueFinder).findTodayIssues(eq(42L), any(Pageable.class));
    }
}
