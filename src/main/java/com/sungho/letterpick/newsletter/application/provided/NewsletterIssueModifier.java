package com.sungho.letterpick.newsletter.application.provided;

public interface NewsletterIssueModifier {

    void delete(Long memberId, Long issueId);
}
