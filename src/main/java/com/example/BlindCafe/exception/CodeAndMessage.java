package com.example.BlindCafe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeAndMessage {

    SIGN_IN("990", "성공적으로 로그인했습니다."),
    SIGN_UP("991", "성공적으로 회원가입이 되었습니다."),
    SIGN_IN_NOT_REQUIRED_INFO("992", "회원가입은 되어있지만 필수 정보가 입력되지 않았습니다"),

    SUCCESS("1000", "성공"),

    INVALID_KAKAO_ACCESS("1002", "카카오 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_KAKAO_TOKEN("1003", "카카오 Access Token이 유효하지 않습니다."),
    FAILED_TO_FIND_AVAILABLE_RSA("1004", "사용 가능한 키가 없습니다."),
    INVALID_APPLE_ACCESS("1005", "애플 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_APPLE_TOKEN("1006", "애플 Identity Token이 유효하지 않습니다."),
    SUSPENDED_USER("1007", "신고로 정지된 유저입니다."),
    NO_USER("1008", "해당되는 유저가 없습니다."),

    INVALID_MAIN_INTEREST("1011", "메인 관심사가 유효하지 않습니다."),
    INVALID_SUB_INTEREST("1012", "세부 관심사가 유효하지 않습니다."),

    NO_DRINK("1020", "해당되는 음료가 없습니다."),
    INVALID_INTEREST_SET("1021", "관심사 설정이 부족합니다."),
    PAST_PARTNER_SEARCH_ERROR("1022", "이전 대화 상대 조회 중 오류가 발생했습니다."),

    NO_MATCHING("1030", "해당되는 매칭이 없습니다."),
    NO_USER_MATCHING("1031", "유효한 매칭이 아닙니다."),

    INVALID_ADDRESS("1040", "유효한 주소가 아닙니다."),

    FCM_JSON_PARSE_ERROR("1050", "FCM JSON 형식이 올바르지 않습니다."),
    FCM_SERVER_ERROR("1051", "FCM 전송 중 오류가 발생했습니다."),

    NOT_REQUIRED_INFO_FOR_MATCHING("1060", "매칭을 요청하기에 충분한 정보가 입력되어 있지 않습니다."),

    INVALID_REASON("1070", "유효하지 않은 이유입니다."),

    INVALID_REQUEST("4000", "잘못된 요청입니다."),
    FAILED_AUTHORIZATION("4001", "검증에 실패했습니다."),
    EXPIRED_TOKEN("4002", "토큰의 기한이 만료되었습니다."),
    FORBIDDEN_AUTHORIZATION("4003", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR("5000", "서버에 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
