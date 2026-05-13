package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.LoginUser;
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
}
