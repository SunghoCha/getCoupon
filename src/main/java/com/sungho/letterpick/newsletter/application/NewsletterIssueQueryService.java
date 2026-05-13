package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.adapter.persistence.NewsletterIssueRepository;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewsletterIssueQueryService implements NewsletterIssueFinder {

    private static final ZoneId TODAY_ZONE = ZoneId.of("Asia/Seoul");

    private final NewsletterIssueRepository newsletterIssueRepository;
    private final Clock clock;

    @Override
    public Slice<NewsletterIssueItem> findTodayIssues(Long memberId, Pageable pageable) {
        NewsletterIssueSearchCondition searchCondition = todaySearchCondition();

        return newsletterIssueRepository.findAllByMemberId(memberId, searchCondition, pageable);
    }

    private NewsletterIssueSearchCondition todaySearchCondition() {
        LocalDate today = LocalDate.now(clock.withZone(TODAY_ZONE));
        Instant receivedFrom = today.atStartOfDay(TODAY_ZONE).toInstant();
        Instant receivedTo = today.plusDays(1).atStartOfDay(TODAY_ZONE).toInstant();

        return new NewsletterIssueSearchCondition(receivedFrom, receivedTo);
    }
}
