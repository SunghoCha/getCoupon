package com.sungho.letterpick.common.exception;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getErrorCode().getStatus())
                .body(ErrorResponse.of(exception));
    }

    /**
     * @PreAuthorize 등 메서드 보안 실패는 Spring Security의 ExceptionTranslationFilter로 넘겨
     * AccessDeniedHandler가 응답 포맷을 책임지도록 한다. 여기서 잡지 말고 rethrow.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void propagateAccessDenied(AccessDeniedException exception) throws AccessDeniedException {
        throw exception;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(
                new ErrorResponse("INVALID_INPUT", message, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("예상치 못한 예외", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "서버 오류가 발생했습니다",
                        Instant.now()
                ));
    }
}
