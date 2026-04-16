package com.sungho.letterpick.member.domain.exception;

import com.sungho.letterpick.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEM-001", "이미 등록된 이메일입니다"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEM-002", "이미 등록된 닉네임입니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEM-003", "회원을 찾을 수 없습니다"),
    INVALID_STATUS(HttpStatus.CONFLICT, "MEM-004", "현재 회원 상태에서는 허용되지 않는 동작입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
