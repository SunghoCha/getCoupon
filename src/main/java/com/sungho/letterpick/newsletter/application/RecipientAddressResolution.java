package com.sungho.letterpick.newsletter.application;

import static java.util.Objects.requireNonNull;

public final class RecipientAddressResolution {

    public enum Type {
        FOUND,
        INVALID_ADDRESS,
        NOT_FOUND
    }

    private final Type type;
    private final Long memberId;

    private RecipientAddressResolution(Type type, Long memberId) {
        this.type = requireNonNull(type);
        this.memberId = memberId;
    }

    public static RecipientAddressResolution found(Long memberId) {
        return new RecipientAddressResolution(Type.FOUND, requireNonNull(memberId));
    }

    public static RecipientAddressResolution invalidAddress() {
        return new RecipientAddressResolution(Type.INVALID_ADDRESS, null);
    }

    public static RecipientAddressResolution notFound() {
        return new RecipientAddressResolution(Type.NOT_FOUND, null);
    }

    public Type type() {
        return type;
    }

    public Long memberId() {
        if (type != Type.FOUND) {
            throw new IllegalStateException("FOUND 결과에서만 memberId를 조회할 수 있습니다.");
        }
        return memberId;
    }
}
