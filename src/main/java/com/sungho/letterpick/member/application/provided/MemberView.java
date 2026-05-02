package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberStatus;

import static java.util.Objects.requireNonNull;

public record MemberView(
        Long memberId,
        String email,
        String nickname,
        MemberStatus status,
        String newsletterInboxAddress
) {
    public static MemberView from(Member member) {
        requireNonNull(member);
        return new MemberView(
                member.getId(),
                member.getEmail().address(),
                member.getNickname().name(),
                member.getStatus(),
                member.getNewsletterInboxAddress().address()
        );
    }
}
