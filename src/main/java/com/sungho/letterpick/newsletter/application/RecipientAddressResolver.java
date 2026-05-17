package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipientAddressResolver {

    private final MemberRepository memberRepository;

    public RecipientAddressResolution resolve(String recipientAddress) {
        Optional<NewsletterInboxAddress> newsletterInboxAddressOpt = NewsletterInboxAddress.tryCreate(recipientAddress);
        if (newsletterInboxAddressOpt.isEmpty()) {
            return RecipientAddressResolution.invalidAddress();
        }

        NewsletterInboxAddress newsletterInboxAddress = newsletterInboxAddressOpt.get();
        return memberRepository.findByNewsletterInboxAddress(newsletterInboxAddress)
                .map(Member::getId)
                .map(RecipientAddressResolution::found)
                .orElseGet(RecipientAddressResolution::notFound);
    }
}

