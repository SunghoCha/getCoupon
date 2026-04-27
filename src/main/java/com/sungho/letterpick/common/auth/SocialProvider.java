package com.sungho.letterpick.common.auth;

import java.util.Arrays;

public enum SocialProvider {
    GOOGLE("google"),
    NAVER("naver");

    private final String registrationId;

    SocialProvider(String registrationId) {
        this.registrationId = registrationId;
    }

    public String registrationId() {
        return registrationId;
    }

    public static SocialProvider fromRegistrationId(String registrationId) {
        return Arrays.stream(values())
                .filter(p -> p.registrationId.equals(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 OAuth2 provider: " + registrationId));
    }
}
