package com.sungho.letterpick.common.auth;

import java.io.Serializable;
import java.util.Objects;

public record SocialUserInfo(
        SocialProvider provider,
        String providerId,
        String email,
        String nickname,
        String profileImageUrl
) implements Serializable {
    public SocialUserInfo {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(providerId, "providerId");
    }
}
