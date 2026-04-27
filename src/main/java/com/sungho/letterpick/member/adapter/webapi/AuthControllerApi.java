package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.common.auth.SocialPrincipal;
import com.sungho.letterpick.member.application.provided.MemberSignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "인증/가입 관련 API")
public interface AuthControllerApi {

    @Operation(
            summary = "OAuth2 신규 회원 가입 완료",
            description = "OAuth2 로그인 후 가입 미완료 상태(ROLE_PENDING_SIGNUP) 사용자가 닉네임을 입력해 가입을 완료한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ROLE_PENDING_SIGNUP 아님)"),
            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
    })
    void signup(SocialPrincipal principal, MemberSignupRequest request);
}
