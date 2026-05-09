package com.sungho.letterpick.newsletter.application.provided;

public interface MemberNewsletterModifier {

    void resubscribe(Long memberId, Long newsletterId);
    void unsubscribe(Long memberId, Long newsletterId);
}
