package com.sungho.letterpick.member.application.provided;

import com.sungho.letterpick.common.auth.SocialProvider;

public record MemberRegisterRequest(
        String email,
        String nickname,
        SocialProvider socialProvider,
        String socialProviderId
) {
}
