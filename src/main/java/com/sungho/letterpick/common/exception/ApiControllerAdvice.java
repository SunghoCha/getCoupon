package com.sungho.letterpick.common.exception;

import com.sungho.letterpick.member.domain.exception.DuplicateEmailException;
import com.sungho.letterpick.member.domain.exception.DuplicateNicknameException;
import com.sungho.letterpick.member.domain.exception.DuplicateSocialIdentityException;
import java.time.Instant;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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
     * 권한 검증 실패는 SecurityConfig의 AccessDeniedHandler가 응답 포맷을 책임지도록 rethrow.
     * 여기서 잡으면 응답 형식이 두 곳으로 갈라진다.
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

    // TODO: 현재는 회원 도메인 unique constraint만 임시로 번역한다.
    //       다른 도메인의 DB constraint 번역이 필요해지면 MemberControllerAdvice 등 도메인별 advice로 분리한다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        BusinessException translated = translateUniqueConstraint(exception);
        if (translated != null) {
            return handleBusinessException(translated);
        }
        return handleUnexpected(exception);
    }

    private BusinessException translateUniqueConstraint(DataIntegrityViolationException exception) {
        String constraintName = findConstraintName(exception);
        if (constraintName == null) {
            return null;
        }

        String normalized = constraintName.toLowerCase(Locale.ROOT);
        if (normalized.contains("uk_member_email")) {
            return new DuplicateEmailException();
        }
        if (normalized.contains("uk_member_nickname")) {
            return new DuplicateNicknameException();
        }
        if (normalized.contains("uk_member_social_identity")) {
            return new DuplicateSocialIdentityException();
        }
        return null;
    }

    private String findConstraintName(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation
                    && constraintViolation.getConstraintName() != null) {
                return constraintViolation.getConstraintName();
            }
            current = current.getCause();
        }
        return exception.getMessage();
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
