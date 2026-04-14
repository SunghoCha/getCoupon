package com.sungho.letterpick.member.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record Nickname(String name) {

    private static final java.util.regex.Pattern ALLOWED_PATTERN = java.util.regex.Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    public Nickname {
        Objects.requireNonNull(name);
        if (name.isBlank() || name.length() < 2 || name.length() > 20 || !ALLOWED_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("닉네임 형식이 올바르지 않습니다.");
        }
    }
}
