package com.sungho.letterpick.newsletter.application.provided;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NewsletterIssueFinder {
    Slice<NewsletterIssueItem> findTodayIssues(Long memberId, Pageable pageable);
    Slice<NewsletterIssueItem> findIssues(Long memberId, String keyword, Pageable pageable);
    NewsletterIssueDetail readIssueDetail(Long memberId, Long issueId);
}
