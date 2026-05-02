package com.sungho.letterpick.member.application;

import com.sungho.letterpick.member.application.provided.*;
import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Email;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import com.sungho.letterpick.member.domain.Nickname;
import com.sungho.letterpick.member.domain.SocialIdentity;
import com.sungho.letterpick.member.domain.exception.DuplicateEmailException;
import com.sungho.letterpick.member.domain.exception.DuplicateNicknameException;
import com.sungho.letterpick.member.domain.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberModifyService implements MemberModifier {
    private static final int NEWSLETTER_INBOX_ADDRESS_GENERATION_MAX_ATTEMPTS = 10;

    private final MemberRepository memberRepository;
    private final NewsletterInboxAddressGenerator newsletterInboxAddressGenerator;

    @Override
    public Member register(MemberRegisterRequest request) {
        Email email = new Email(request.email());
        Nickname nickname = new Nickname(request.nickname());
        SocialIdentity socialIdentity = new SocialIdentity(request.socialProvider(), request.socialProviderId());

        // TODO: race condition — exists 검증과 commit 사이 동시성 문제. 현재는 DB UNIQUE 제약이 최종 방어.
        //       HTTP 도입 시 @ControllerAdvice에서 DataIntegrityViolationException → Duplicate…Exception 으로 번역할 것.
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }

        NewsletterInboxAddress newsletterInboxAddress = generateUniqueNewsletterInboxAddress();
        Member member = Member.register(email, nickname, socialIdentity, newsletterInboxAddress);
        return memberRepository.save(member);
    }

    @Override
    public void changeNickname(Long memberId, MemberNicknameChangeRequest request) {
        Member member = findMember(memberId);
        member.ensureCanChangeNickname();
        Nickname nickname = new Nickname(request.nickname());

        // TODO: race condition — exists 검증과 commit 사이 동시성 문제. 현재는 DB UNIQUE 제약이 최종 방어.
        //       더티 체킹이라 서비스 내부에서 잡기 어려움. HTTP 도입 시 @ControllerAdvice에서 번역할 것.
        if (!Objects.equals(member.getNickname(), nickname)
                && memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }

        member.changeNickname(nickname);
    }

    @Override
    public void withdraw(Long requesterId) {
        Member member = findMember(requesterId);
        member.withdraw();
    }

    @Override
    public void suspend(MemberSuspendRequest request) {
        Member member = findMember(request.memberId());
        member.suspend();
    }

    @Override
    public void withdrawByAdmin(MemberWithdrawByAdminRequest request) {
        Member member = findMember(request.memberId());
        member.withdrawByAdmin();
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private NewsletterInboxAddress generateUniqueNewsletterInboxAddress() {
        for (int attempt = 0; attempt < NEWSLETTER_INBOX_ADDRESS_GENERATION_MAX_ATTEMPTS; attempt++) {
            NewsletterInboxAddress newsletterInboxAddress = newsletterInboxAddressGenerator.generate();
            if (!memberRepository.existsByNewsletterInboxAddress(newsletterInboxAddress)) {
                return newsletterInboxAddress;
            }
        }

        throw new IllegalStateException("뉴스레터 수신 주소를 생성하지 못했습니다.");
    }
}
