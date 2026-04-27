package com.sungho.letterpick.member.domain;

import com.sungho.letterpick.common.auth.SocialProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public record SocialIdentity(
        @Enumerated(EnumType.STRING)
        SocialProvider socialProvider,
        String socialProviderId
) {
    public SocialIdentity {
        Objects.requireNonNull(socialProvider);
        Objects.requireNonNull(socialProviderId);
        if (socialProviderId.isBlank()) {
            throw new IllegalArgumentException("소셜 식별자 형식이 올바르지 않습니다.");
        }
    }
}
