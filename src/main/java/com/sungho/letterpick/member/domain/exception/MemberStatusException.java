package com.sungho.letterpick.member.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class MemberStatusException extends BusinessException {
    public MemberStatusException() {
        super(MemberErrorCode.INVALID_STATUS);
    }
}
