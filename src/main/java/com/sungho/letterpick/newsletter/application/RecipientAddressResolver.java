package com.sungho.letterpick.newsletter.application;


import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
    지금은 뉴스레터 서비스가 멤버레포지토리 의존하지만
    나중 분리가능성 생각해서 별도 클래스로 둠
*/
@Service
@RequiredArgsConstructor
public class RecipientAddressResolver {

    private final MemberRepository memberRepository;

    public Optional<Long> resolveMemberId(String recipientAddress) {
        return memberRepository.findByNewsletterInboxAddress(new NewsletterInboxAddress(recipientAddress))
                .map(Member::getId);
    }
}

