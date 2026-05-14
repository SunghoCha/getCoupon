package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterIssueDetailResponse;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterIssuesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Newsletter Issue", description = "회원 뉴스레터 이슈 API")
public interface NewsletterIssueControllerApi {

    @Operation(
            summary = "오늘 도착한 뉴스레터 이슈 목록 조회",
            description = "로그인한 회원이 투데이 탭에서 오늘 도착한 자신의 뉴스레터 이슈 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 query parameter 형식 오류")
    })
    NewsletterIssuesResponse getTodayIssues(LoginUser loginUser, Pageable pageable);

    @Operation(
            summary = "뉴스레터 이슈 상세 조회",
            description = "로그인한 회원이 자신의 뉴스레터 이슈 상세를 조회하고, 상세 조회 성공 시 해당 이슈를 읽음 상태로 변경한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이슈 없음")
    })
    NewsletterIssueDetailResponse getIssueDetail(LoginUser loginUser, Long issueId);

    @Operation(
            summary = "뉴스레터 이슈 삭제",
            description = "로그인한 회원이 자신의 뉴스레터 이슈를 삭제 처리한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "이슈 없음")
    })
    void deleteIssue(LoginUser loginUser, Long issueId);
}
