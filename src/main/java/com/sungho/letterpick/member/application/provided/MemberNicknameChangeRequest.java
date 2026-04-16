package com.sungho.letterpick.member.application.provided;

public record MemberNicknameChangeRequest(
        Long memberId,
        String nickname
) {
}
