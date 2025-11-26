package com.kidk.api.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth (인증 관련)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_003", "접근 권한이 없습니다."),

    // User (사용자 관련)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 사용자입니다."),

    // Family (가족 관련)
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_001", "가족 정보를 찾을 수 없습니다."),
    ALREADY_IN_FAMILY(HttpStatus.CONFLICT, "FAMILY_002", "이미 가족에 소속되어 있습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "FAMILY_003", "유효하지 않은 초대 코드입니다."),
    INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "FAMILY_004", "초대 코드가 만료되었습니다. (유효기간 7일 경과)"),

    // Account (계좌 관련)
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_001", "계좌를 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "ACCOUNT_002", "잔액이 부족합니다."),
    SAME_ACCOUNT_TRANSFER(HttpStatus.BAD_REQUEST, "ACCOUNT_003", "동일한 계좌로 이체할 수 없습니다."),

    // Mission & Verification
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_001", "미션을 찾을 수 없습니다."),
    ALREADY_COMPLETED_MISSION(HttpStatus.BAD_REQUEST, "MISSION_002", "이미 완료된 미션입니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "VERIFICATION_001", "인증 내역을 찾을 수 없습니다."),
    ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "VERIFICATION_002", "이미 검토된 인증입니다."),

    // Common (공통)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "SYS_001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}