package com.sungho.letterpick.member.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(MemberErrorCode.DUPLICATE_EMAIL);
    }
}
