package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.adapter.persistence.MemberNewsletterRepository;
import com.sungho.letterpick.newsletter.adapter.persistence.NewslettersRepository;
import com.sungho.letterpick.newsletter.application.provided.MemberNewsletterModifier;
import com.sungho.letterpick.newsletter.domain.MemberNewsletter;
import com.sungho.letterpick.newsletter.domain.exception.MemberNewsletterNotFoundException;
import com.sungho.letterpick.newsletter.domain.exception.NewsletterNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberNewsletterModifyService implements MemberNewsletterModifier {

    private final MemberNewsletterRepository memberNewsletterRepository;
    private final NewslettersRepository newslettersRepository;

    @Override
    public void resubscribe(Long memberId, Long newsletterId) {
        if (!newslettersRepository.existsById(newsletterId)) {
            throw new NewsletterNotFoundException();
        }

        MemberNewsletter memberNewsletter = memberNewsletterRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .orElseThrow(MemberNewsletterNotFoundException::new);
        memberNewsletter.resubscribe();
    }

    @Override
    public void unsubscribe(Long memberId, Long newsletterId) {
        if (!newslettersRepository.existsById(newsletterId)) {
            throw new NewsletterNotFoundException();
        }

        MemberNewsletter memberNewsletter = memberNewsletterRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .orElseThrow(MemberNewsletterNotFoundException::new);
        memberNewsletter.unsubscribe();
    }
}
