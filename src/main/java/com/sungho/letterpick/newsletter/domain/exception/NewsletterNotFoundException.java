package com.sungho.letterpick.newsletter.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class NewsletterNotFoundException extends BusinessException {

    public NewsletterNotFoundException() {
        super(NewsletterErrorCode.NEWSLETTER_NOT_FOUND);
    }
}
