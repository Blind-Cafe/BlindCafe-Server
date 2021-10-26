package com.example.BlindCafe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeAndMessage {

    SIGN_IN("1000", "성공적으로 로그인했습니다."),
    SIGN_UP("1001", "성공적으로 회원가입이 되었습니다."),
    INVALID_KAKAO_ACCESS("1002", "카카오 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_KAKAO_TOKEN("1003", "카카오 Access Token이 유효하지 않습니다."),
    INVALID_APPLE_ACCESS("1005", "애플 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_APPLE_TOKEN("1006", "애플 Identity Token이 유효하지 않습니다."),
    SUSPENDED_USER("1007", "신고로 정지된 유저입니다."),

    INVALID_REQUEST("4000", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR("5000", "서버에 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
