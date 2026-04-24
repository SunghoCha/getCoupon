package com.sungho.letterpick.common.exception;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(BusinessException exception) {
        return new ErrorResponse(
                exception.getErrorCode().getCode(),
                exception.getMessage(),
                Instant.now()
        );
    }
}
