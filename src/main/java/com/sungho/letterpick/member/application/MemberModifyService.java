package com.sungho.letterpick.member.application;

import com.sungho.letterpick.member.application.provided.MemberNicknameChangeRequest;
import com.sungho.letterpick.member.application.provided.MemberRegister;
import com.sungho.letterpick.member.application.provided.MemberRegisterRequest;
import com.sungho.letterpick.member.application.required.MemberRepository;
import com.sungho.letterpick.member.domain.Email;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.Nickname;
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
public class MemberModifyService implements MemberRegister {

    private final MemberRepository memberRepository;

    @Override
    public Member register(MemberRegisterRequest request) {
        Email email = new Email(request.email());
        Nickname nickname = new Nickname(request.nickname());
        // TODO: race condition — exists 검증과 commit 사이 동시성 문제. 현재는 DB UNIQUE 제약이 최종 방어.
        //       HTTP 도입 시 @ControllerAdvice에서 DataIntegrityViolationException → Duplicate…Exception 으로 번역할 것.
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }

        Member member = Member.register(email, nickname);
        return memberRepository.save(member);
    }

    @Override
    public void changeNickname(MemberNicknameChangeRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(MemberNotFoundException::new);

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
        Member member = memberRepository.findById(requesterId)
                .orElseThrow(MemberNotFoundException::new);
        member.withdraw();
    }
}
