package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.CurrentUser;
import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterIssueDetailResponse;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterIssuesResponse;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueDetail;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/me/newsletter-issues")
@RestController
@RequiredArgsConstructor
public class NewsletterIssueController implements NewsletterIssueControllerApi {

    private final NewsletterIssueFinder newsletterIssueFinder;

    @Override
    @GetMapping("/today")
    public NewsletterIssuesResponse getTodayIssues(
            @CurrentUser LoginUser loginUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Slice<NewsletterIssueItem> issueItems = newsletterIssueFinder.findTodayIssues(loginUser.memberId(), pageable);
        return NewsletterIssuesResponse.from(issueItems);
    }

    @Override
    @GetMapping("/{issueId}")
    public NewsletterIssueDetailResponse getIssueDetail(@CurrentUser LoginUser loginUser,
                                                        @PathVariable("issueId") Long issueId) {
        NewsletterIssueDetail issueDetail = newsletterIssueFinder.readIssueDetail(loginUser.memberId(), issueId);
        return NewsletterIssueDetailResponse.from(issueDetail);
    }
}
