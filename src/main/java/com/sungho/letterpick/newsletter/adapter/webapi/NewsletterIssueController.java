package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.CurrentUser;
import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterIssueDetailResponse;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterIssuesResponse;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueDetail;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueModifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/me/newsletter-issues")
@RestController
@RequiredArgsConstructor
public class NewsletterIssueController implements NewsletterIssueControllerApi {

    private final NewsletterIssueFinder newsletterIssueFinder;
    private final NewsletterIssueModifier newsletterIssueModifier;

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
    @GetMapping
    public NewsletterIssuesResponse getIssues(
            @CurrentUser LoginUser loginUser,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Slice<NewsletterIssueItem> issueItems = newsletterIssueFinder.findIssues(loginUser.memberId(), pageable);
        return NewsletterIssuesResponse.from(issueItems);
    }

    @Override
    @GetMapping("/{issueId}")
    public NewsletterIssueDetailResponse getIssueDetail(@CurrentUser LoginUser loginUser,
                                                        @PathVariable("issueId") Long issueId) {
        NewsletterIssueDetail issueDetail = newsletterIssueFinder.readIssueDetail(loginUser.memberId(), issueId);
        return NewsletterIssueDetailResponse.from(issueDetail);
    }

    @Override
    @DeleteMapping("/{issueId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteIssue(
            @CurrentUser LoginUser loginUser,
            @PathVariable("issueId") Long issueId
    ) {
        newsletterIssueModifier.delete(loginUser.memberId(), issueId);
    }
}
