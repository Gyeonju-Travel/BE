package com.example.gyeonjutravel.domain.member.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements BaseErrorCode {

    PASSWORD_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER_400_1", "비밀번호 확인이 일치하지 않습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "MEMBER_400_2", "인증번호가 올바르지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "MEMBER_400_3", "인증번호가 만료되었습니다."),
    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "MEMBER_400_4", "비밀번호 재설정 토큰이 올바르지 않습니다."),
    EXPIRED_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "MEMBER_400_5", "비밀번호 재설정 토큰이 만료되었습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_409_1", "이미 가입된 이메일입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "MEMBER_401_1", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_AUTHORIZATION_HEADER(HttpStatus.UNAUTHORIZED, "MEMBER_401_2", "Authorization 헤더가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER_401_3", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER_401_4", "만료된 토큰입니다."),
    LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER_401_5", "로그아웃된 토큰입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_1", "회원을 찾을 수 없습니다."),
    VERIFICATION_EMAIL_SEND_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "MEMBER_500_1",
            "인증번호 이메일 발송에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    MemberErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
