package com.sungho.letterpick.newsletter.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class MemberNewsletterNotFoundException extends BusinessException {

    public MemberNewsletterNotFoundException() {
        super(NewsletterErrorCode.MEMBER_NEWSLETTER_NOT_FOUND);
    }
}
