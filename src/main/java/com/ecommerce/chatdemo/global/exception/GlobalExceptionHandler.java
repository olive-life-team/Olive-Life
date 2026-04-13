package com.ecommerce.chatdemo.global.exception;

import com.ecommerce.chatdemo.domain.member.entity.MemberRole;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 서비스 계층에서 던진 비즈니스 예외를 한 곳에서 처리한다.
     * 채팅방 없음, 참여자 아님, 락 충돌 같은 예외를 일관된 응답으로 내리기 위해 필요하다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * DTO 검증 실패 처리
     * 회원가입, 로그인, 검색 요청 파라미터 검증 시 자주 발생한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : CommonErrorCode.INVALID_INPUT_VALUE.getMessage();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), message));
    }

    /**
     * 분산 락 획득 실패 처리
     * 과제 필수 기능에서 Redis Lock이 들어가므로 미리 구조를 잡아두는 것이 좋다.
     */
    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockAcquisitionException(
            LockAcquisitionException e,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(CommonErrorCode.CONFLICT.getCode(), e.getMessage()));
    }

    /**
     * 권한 부족 예외 처리
     * PreAuthorize 같은 메서드 보안에서 권한이 없을 때 403으로 응답한다.
     * 콘솔에는 스택트레이스 대신 한 줄 로그만 남겨서 확인하기 쉽게 한다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(
                        CommonErrorCode.FORBIDDEN.getCode(),
                        CommonErrorCode.FORBIDDEN.getMessage()
                ));
    }

    /**
     * 인증 정보 없음 예외 처리
     * 로그인하지 않은 상태에서 보호된 API에 접근하면 401로 응답한다.
     * 권한 문제와 구분해서 확인할 수 있도록 별도로 분리한다.
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException e
    ) {
        log.warn("인증 필요: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(
                        CommonErrorCode.UNAUTHORIZED.getCode(),
                        CommonErrorCode.UNAUTHORIZED.getMessage()
                ));
    }

    /**
     * 예상하지 못한 예외는 500으로 묶어 처리한다.
     * 내부 구현 정보가 그대로 노출되지 않도록 일반 메시지로 감싼다.
     * 이 경우에는 실제 원인 파악이 필요하므로 스택트레이스를 로그에 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("unexpected error", e);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }

    /**
     * role 필드에 enum에 없는 값이 들어온 경우를 처리한다.
     * 회원가입 요청에서 잘못된 role 값이 들어오면 안내 메시지를 반환한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            boolean isRoleField = invalidFormatException.getPath().stream()
                    .anyMatch(reference -> "role".equals(reference.getFieldName()));

            Class<?> targetType = invalidFormatException.getTargetType();

            if (isRoleField && targetType != null && MemberRole.class.isAssignableFrom(targetType)) {
                return ResponseEntity
                        .status(AuthErrorCode.INVALID_ROLE_VALUE.getStatus())
                        .body(ApiResponse.fail(
                                AuthErrorCode.INVALID_ROLE_VALUE.getCode(),
                                AuthErrorCode.INVALID_ROLE_VALUE.getMessage()
                        ));
            }
        }

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(
                        CommonErrorCode.INVALID_INPUT_VALUE.getCode(),
                        CommonErrorCode.INVALID_INPUT_VALUE.getMessage()
                ));
    }
}