package com.sungho.letterpick.member.domain;

import com.sungho.letterpick.common.auth.SocialProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class SocialIdentity {

    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    private String socialProviderId;

    protected SocialIdentity() {
    }

    public SocialIdentity(SocialProvider socialProvider, String socialProviderId) {
        Objects.requireNonNull(socialProvider);
        Objects.requireNonNull(socialProviderId);
        if (socialProviderId.isBlank()) {
            throw new IllegalArgumentException("소셜 식별자 형식이 올바르지 않습니다.");
        }
        this.socialProvider = socialProvider;
        this.socialProviderId = socialProviderId;
    }

    public SocialProvider socialProvider() {
        return socialProvider;
    }

    public String socialProviderId() {
        return socialProviderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SocialIdentity that)) {
            return false;
        }
        return socialProvider == that.socialProvider
                && Objects.equals(socialProviderId, that.socialProviderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socialProvider, socialProviderId);
    }
}
