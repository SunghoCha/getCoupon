package com.sungho.letterpick.member.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Embeddable
public class NewsletterInboxAddress {
    public static final int TOKEN_LENGTH = 12;

    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile("^[a-z0-9]{" + TOKEN_LENGTH + "}$");
    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile("^(?=.{1,253}$)(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}$");

    private String address;

    protected NewsletterInboxAddress() {
    }

    public static Optional<NewsletterInboxAddress> tryCreate(String address) {
        if (!isValid(address)) {
            return Optional.empty();
        }
        return Optional.of(new NewsletterInboxAddress(address));
    }

    public NewsletterInboxAddress(String address) {
        Objects.requireNonNull(address);
        if (!isValid(address)) {
            throw new IllegalArgumentException("뉴스레터 수신 주소 형식이 바르지 않습니다.");
        }
        this.address = address;
    }

    public String address() {
        return address;
    }

    private static boolean isValid(String address) {
        if (address == null) {
            return false;
        }
        String[] parts = address.split("@", -1);
        return parts.length == 2
                && LOCAL_PART_PATTERN.matcher(parts[0]).matches()
                && DOMAIN_PATTERN.matcher(parts[1]).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NewsletterInboxAddress that)) {
            return false;
        }
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
