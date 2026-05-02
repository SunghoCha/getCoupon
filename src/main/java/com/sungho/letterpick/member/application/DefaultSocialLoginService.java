package com.sungho.letterpick.member.application;

import com.sungho.letterpick.member.application.provided.SocialLoginService;
import com.sungho.letterpick.member.adapter.persistence.MemberRepository;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.SocialIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultSocialLoginService implements SocialLoginService {

    private final MemberRepository memberRepository;

    @Override
    public Optional<Member> findExistingMember(SocialIdentity identity) {
        return memberRepository.findBySocialIdentity(identity);
    }

    @Override
    public boolean canLogin(Member member) {
        return member.canLogin();
    }
}
