package com.sungho.letterpick.member.application;

import com.sungho.letterpick.member.application.provided.MemberRegister;
import com.sungho.letterpick.member.application.provided.MemberRegisterRequest;
import com.sungho.letterpick.member.application.required.MemberRepository;
import com.sungho.letterpick.member.domain.Email;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.Nickname;
import com.sungho.letterpick.member.domain.exception.DuplicateEmailException;
import com.sungho.letterpick.member.domain.exception.DuplicateNicknameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberModifyService implements MemberRegister {

    private final MemberRepository memberRepository;

    @Override
    public Member register(MemberRegisterRequest request) {
        Email email = new Email(request.email());
        Nickname nickname = new Nickname(request.nickname());
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateNicknameException();
        }

        Member member = Member.register(email, nickname);
        return memberRepository.save(member);
    }
}
