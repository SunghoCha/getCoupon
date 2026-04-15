package com.sungho.letterpick.member.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException() {
        super(MemberErrorCode.DUPLICATE_NICKNAME);
    }
}
