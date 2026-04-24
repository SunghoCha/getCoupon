package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.common.auth.LoginUser;
import com.sungho.letterpick.member.application.provided.MemberNicknameChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member", description = "회원 관련 API")
public interface MemberControllerApi {

    @Operation(
            summary = "본인 탈퇴",
            description = "로그인한 회원이 자신의 계정을 탈퇴 처리한다. 상태는 DEACTIVATED로 전이된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "현재 상태에서 탈퇴 불가 (ACTIVE가 아님)")
    })
    void withdraw(LoginUser loginUser);

    @Operation(
            summary = "회원 정보 수정",
            description = "로그인한 회원이 자신의 정보를 수정한다. 현재 수정 가능 필드는 nickname."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "닉네임 중복 또는 상태 위반")
    })
    void changeNickname(LoginUser loginUser, MemberNicknameChangeRequest request);
}
