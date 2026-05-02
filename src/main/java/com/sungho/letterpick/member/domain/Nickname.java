package com.sungho.letterpick.member.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Nickname {

    private static final java.util.regex.Pattern ALLOWED_PATTERN = java.util.regex.Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    private String name;

    protected Nickname() {
    }

    public Nickname(String name) {
        Objects.requireNonNull(name);
        if (name.isBlank() || name.length() < 2 || name.length() > 20 || !ALLOWED_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("닉네임 형식이 올바르지 않습니다.");
        }
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Nickname nickname)) {
            return false;
        }
        return Objects.equals(name, nickname.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
