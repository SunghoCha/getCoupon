package com.sungho.letterpick.member.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class DuplicateSocialIdentityException extends BusinessException {

    public DuplicateSocialIdentityException() {
        super(MemberErrorCode.DUPLICATE_SOCIAL_IDENTITY);
    }
}
