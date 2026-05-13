package com.sungho.letterpick.newsletter.domain.exception;

import com.sungho.letterpick.common.exception.BusinessException;

public class NewsletterIssueNotFoundException extends BusinessException {

    public NewsletterIssueNotFoundException() {
        super(NewsletterErrorCode.NEWSLETTER_ISSUE_NOT_FOUND);
    }
}
