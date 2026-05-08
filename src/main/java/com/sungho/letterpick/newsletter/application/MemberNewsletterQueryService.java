package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.adapter.persistence.MemberNewsletterRepository;
import com.sungho.letterpick.newsletter.adapter.persistence.NewslettersRepository;
import com.sungho.letterpick.newsletter.application.provided.MemberNewsletterFinder;
import com.sungho.letterpick.newsletter.domain.MemberNewsletter;
import com.sungho.letterpick.newsletter.domain.Newsletter;
import com.sungho.letterpick.newsletter.domain.exception.NewsletterNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberNewsletterQueryService implements MemberNewsletterFinder {

    private final MemberNewsletterRepository memberNewsletterRepository;
    private final NewslettersRepository newslettersRepository;

    @Override
    public SubscriptionInfo findSubscriptionInfo(Long memberId, Long newsletterId) {
        Newsletter newsletter = newslettersRepository.findById(newsletterId)
                .orElseThrow(NewsletterNotFoundException::new);

        return memberNewsletterRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .map(this::toSubscriptionInfo)
                .orElseGet(() -> SubscriptionInfo.none(newsletter.getSubscribeUrl()));

    }

    private SubscriptionInfo toSubscriptionInfo(MemberNewsletter memberNewsletter) {
        if (memberNewsletter.isUnsubscribed()) {
            return SubscriptionInfo.unsubscribed();
        }
        return SubscriptionInfo.active();
    }
}
