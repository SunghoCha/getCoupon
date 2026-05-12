package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.adapter.persistence.NewsletterIssueRepository;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueSearchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NewsletterIssueQueryServiceTest {

    @Mock
    private NewsletterIssueRepository newsletterIssueRepository;

    @Test
    @DisplayName("Asia/Seoul 기준 오늘 범위로 회원의 뉴스레터 이슈를 조회한다")
    void findTodayIssues_queries_member_issues_with_today_range_in_asia_seoul() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Clock clock = Clock.fixed(Instant.parse("2050-05-12T03:00:00Z"), ZoneOffset.UTC);
        NewsletterIssueQueryService newsletterIssueQueryService =
                new NewsletterIssueQueryService(newsletterIssueRepository, clock);
        Slice<NewsletterIssueItem> expectedResult = new SliceImpl<>(List.of(), pageable, false);

        given(newsletterIssueRepository.findAllByMemberId(
                eq(memberId),
                any(NewsletterIssueSearchCondition.class),
                eq(pageable)
        )).willReturn(expectedResult);

        // when
        Slice<NewsletterIssueItem> result = newsletterIssueQueryService.findTodayIssues(memberId, pageable);

        // then
        ArgumentCaptor<NewsletterIssueSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(NewsletterIssueSearchCondition.class);
        verify(newsletterIssueRepository).findAllByMemberId(eq(memberId), conditionCaptor.capture(), eq(pageable));

        NewsletterIssueSearchCondition condition = conditionCaptor.getValue();
        assertThat(condition.receivedFrom()).isEqualTo(Instant.parse("2050-05-11T15:00:00Z"));
        assertThat(condition.receivedTo()).isEqualTo(Instant.parse("2050-05-12T15:00:00Z"));
        assertThat(result).isSameAs(expectedResult);
    }
}
