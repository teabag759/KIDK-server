package com.kidk.api.global.exception;

import com.kidk.api.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 현재 활성화된 프로파일 확인 (application.yml의 spring.profiles.active 값)
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    // 1. CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        log.warn("CustomException Occurred: {}", e.getErrorCode().getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getErrorCode()));
    }

    // 2. @Valid 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("Validation Failed: {}", errorMessage);
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(), errorMessage));
    }

    // 3. 그 외 모든 에러 (500) - 상세 정보 포함 로직 추가
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);

        // 개발(test) 환경이거나 로컬인 경우 상세 정보 반환
        if ("test".equals(activeProfile) || "local".equals(activeProfile) || "dev".equals(activeProfile)) {
            return ResponseEntity
                    .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                    .body(ApiResponse.fail(
                            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            e.getClass().getSimpleName() + ": " + e.getMessage() // 예외 클래스와 메시지 노출
                    ));
        }

        // 운영 환경에서는 일반적인 메시지 반환
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}