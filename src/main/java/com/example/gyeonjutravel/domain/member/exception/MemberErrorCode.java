package com.example.gyeonjutravel.domain.member.exception;

import com.example.gyeonjutravel.global.apiPayload.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements BaseErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_409_1", "이미 가입된 이메일입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "MEMBER_401_1", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_AUTHORIZATION_HEADER(HttpStatus.UNAUTHORIZED, "MEMBER_401_2", "Authorization 헤더가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER_401_3", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER_401_4", "만료된 토큰입니다."),
    LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "MEMBER_401_5", "로그아웃된 토큰입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_1", "회원을 찾을 수 없습니다.");

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
