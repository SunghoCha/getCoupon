package com.sungho.letterpick.member.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}
