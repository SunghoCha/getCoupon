package com.sungho.letterpick.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(BusinessException exception) {
        return new ErrorResponse(
                exception.getErrorCode().getCode(),
                exception.getMessage(),
                LocalDateTime.now()
        );
    }
}
