package com.sungho.letterpick.newsletter.application.provided;

import com.sungho.letterpick.newsletter.application.SubscriptionInfo;

public interface MemberNewsletterFinder {

    SubscriptionInfo findSubscriptionInfo(Long memberId, Long newsletterId);
}
