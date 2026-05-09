package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.SubscriptionInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Newsletter Subscription", description = "회원 뉴스레터 구독 관리 API")
public interface MemberNewsletterControllerApi {

    @Operation(
            summary = "내 뉴스레터 구독 정보 조회",
            description = "로그인한 회원이 특정 뉴스레터에 대한 자신의 구독 상태를 조회한다. 미구독 상태이면 외부 구독 URL도 함께 반환한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음")
    })
    SubscriptionInfoResponse getSubscriptionInfo(LoginUser loginUser, Long newsletterId);

    @Operation(
            summary = "뉴스레터 재구독",
            description = "로그인한 회원이 구독 해지 상태의 뉴스레터를 다시 구독한다. 앱 내부 구독 상태를 ACTIVE로 전이한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "재구독 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터 또는 구독 이력을 찾을 수 없음")
    })
    void resubscribe(LoginUser loginUser, Long newsletterId);

    @Operation(
            summary = "뉴스레터 구독 해지",
            description = "로그인한 회원이 앱 안에서 특정 뉴스레터를 구독 해지한다. 외부 뉴스레터 사이트의 실제 구독 해지는 수행하지 않는다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "구독 해지 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터 또는 구독 이력을 찾을 수 없음")
    })
    void unsubscribe(LoginUser loginUser, Long newsletterId);
}
