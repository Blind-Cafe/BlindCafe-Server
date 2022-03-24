package com.example.BlindCafe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeAndMessage {

    INVALID_SOCIAL_PLATFORM("1001", "소셜 플랫폼이 유효하지 않습니다"),
    INVALID_KAKAO_ACCESS("1002", "카카오 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_KAKAO_TOKEN("1003", "카카오 Access Token이 유효하지 않습니다."),
    FAILED_TO_FIND_AVAILABLE_RSA("1004", "사용 가능한 키가 없습니다."),
    INVALID_APPLE_ACCESS("1005", "애플 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_APPLE_TOKEN("1006", "애플 Identity Token이 유효하지 않습니다."),
    SUSPENDED_USER("1007", "신고로 정지된 유저입니다."),
    EMPTY_USER("1008", "해당되는 유저가 없습니다."),
    RETIRED_USER("1009", "탈퇴한 유저입니다."),
    ALREADY_REQUIRED_INFO("1010", "이미 추가 정보를 입력받았습니다."),
    DUPLICATED_PHONE_NUMBER("1013", "전화번호가 중복됩니다."),
    INVALID_PHONE_NUMBER("1014", "전화번호 형식이 올바르지 않습니다."),

    INVALID_MAIN_INTEREST("1011", "메인 관심사가 유효하지 않습니다."),
    INVALID_SUB_INTEREST("1012", "세부 관심사가 유효하지 않습니다."),

    EMPTY_DRINK("1020", "해당되는 음료가 없습니다."),
    ALREADY_SELECT_DRINK("1023", "이미 음료를 선택했습니다."),
    INVALID_INTEREST_SET("1021", "관심사 설정이 부족합니다."),
    PAST_PARTNER_SEARCH_ERROR("1022", "이전 대화 상대 조회 중 오류가 발생했습니다."),

    EMPTY_MATCHING("1030", "해당되는 매칭이 없습니다."),
    NON_AUTHORIZATION_MATCHING("1031", "권한이 없는 매칭입니다."),
    INVALID_MATCHING("1032", "매칭 상태가 유효하지 않습니다."),
    REQUEST_EXPIRED("1033", "요청 대기 시간 초과로 인해 요청이 취소되었습니다."),
    ALREADY_MATCHING_REQUEST("1034", "이미 매칭 요청 중입니다."),
    LACK_OF_TICKET("1035", "매칭권이 부족합니다."),
    EMPTY_MATCHING_REQUEST("1100", "현재 요청하고 있는 매칭이 없습니다."),

    INVALID_ADDRESS("1040", "유효한 주소가 아닙니다."),

    FCM_JSON_PARSE_ERROR("1050", "FCM JSON 형식이 올바르지 않습니다."),
    FCM_SERVER_ERROR("1051", "FCM 전송 중 오류가 발생했습니다."),

    NOT_REQUIRED_INFO_FOR_MATCHING("1060", "매칭을 요청하기에 충분한 정보가 입력되어 있지 않습니다."),

    EMPTY_REASON("1070", "유효하지 않은 이유입니다."),

    FILE_CONVERT_ERROR("1080", "파일 변환에 실패했습니다."),
    FILE_EXTENSION_ERROR("1081", "파일 확장자 인식에 실패했습니다."),

    INVALID_PROFILE_IMAGE_SEQUENCE("1090", "유효하지 않은 우선순위입니다."),



    NOT_YET_EXCHANGE_PROFILE("1110", "프로필 교환을 할 수 있는 매칭이 아닙니다."),

    EXCEED_MATCHING_TOPIC("1120", "더 이상 존재하는 토픽이 없습니다."),
    INVALID_TOPIC("1121", "현재 유효하지 않은 토픽입니다. "),

    NOT_YET_PROFILE_OPEN("1130", "상대방이 프로필 작성 중"),
    REJECT_PROFILE_EXCHANGE("1131", "프로필 교환이 거절된 매칭입니다."),

    EMPTY_PROFILE_IMAGE("1140", "삭제할 프로필 사진이 없습니다."),

    EMPTY_MESSAGE_TYPE("1150", "지원하지 않는 메세지 형식입니다."),

    FIREBASE_CREDENTIALS_ERROR("1160", "Firebase 인증 에러"),
    FIREBASE_INSERT_ERROR("1161", "Firebase 데이터 저장 에러" ),

    TOPIC_SERVE_THREAD_ERROR("1170", "토픽 제공 스레드 에러입니다."),

    DUPLICATED_MATCHING_REQUEST("1180", "이미 매칭 요청 중입니다."),

    EMAIL_SEND_ERROR("1200", "이메일 전송 중 에러가 발생했습니다."),

    BAD_REQUEST("4000", "잘못된 요청입니다."),
    FAILED_AUTHORIZATION("4001", "검증에 실패했습니다."),
    EXPIRED_TOKEN("4002", "토큰의 기한이 만료되었습니다."),
    FORBIDDEN_AUTHORIZATION("4003", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR("5000", "서버에 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
