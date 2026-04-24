package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.member.application.provided.MemberSuspendRequest;
import com.sungho.letterpick.member.application.provided.MemberWithdrawByAdminRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin - Member", description = "관리자 회원 관리 API")
public interface AdminMemberControllerApi {

    @Operation(
            summary = "회원 정지",
            description = "관리자가 ACTIVE 상태의 회원을 정지한다. 상태는 SUSPENDED로 전이된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "정지 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "현재 상태에서 정지 불가 (ACTIVE가 아님)")
    })
    void suspend(MemberSuspendRequest request);

    @Operation(
            summary = "관리자 회원 탈퇴 처리",
            description = "관리자가 SUSPENDED 상태의 회원을 탈퇴 처리한다. 상태는 DEACTIVATED로 전이된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 처리 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "현재 상태에서 탈퇴 처리 불가 (SUSPENDED가 아님)")
    })
    void withdrawByAdmin(MemberWithdrawByAdminRequest request);
}
