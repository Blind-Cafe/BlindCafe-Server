package com.example.BlindCafe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeAndMessage {

    // Auth
    INVALID_SOCIAL_PLATFORM("1001", "소셜 플랫폼이 유효하지 않습니다"),
    INVALID_KAKAO_ACCESS("1002", "카카오 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_KAKAO_TOKEN("1003", "카카오 Access Token이 유효하지 않습니다."),
    FAILED_TO_FIND_AVAILABLE_RSA("1004", "사용 가능한 키가 없습니다."),
    INVALID_APPLE_ACCESS("1005", "애플 로그인 서버에 접근 중 예외가 발생했습니다."),
    INVALID_APPLE_TOKEN("1006", "애플 Identity Token이 유효하지 않습니다."),

    // User
    SUSPENDED_USER("1100", "신고로 정지된 유저입니다."),
    EMPTY_USER("1101", "해당되는 유저가 없습니다."),
    RETIRED_USER("1102", "탈퇴한 유저입니다."),
    ALREADY_REQUIRED_INFO("1103", "이미 추가 정보를 입력받았습니다."),
    DUPLICATED_PHONE_NUMBER("1104", "전화번호가 중복됩니다."),
    INVALID_PHONE_NUMBER("1105", "전화번호 형식이 올바르지 않습니다."),
    INVALID_NICKNAME("1106", "'관리자' 또는 '매니저' 키워드를 포함하는 닉네임을 사용할 수 없습니다."),
    INVALID_MAIN_INTEREST("1107", "메인 관심사가 유효하지 않습니다."),

    // S3 Util
    FILE_CONVERT_ERROR("1200", "파일 변환에 실패했습니다."),
    FILE_EXTENSION_ERROR("1201", "파일 확장자 인식에 실패했습니다."),

    // Matching
    EMPTY_DRINK("1300", "해당되는 음료가 없습니다."),
    ALREADY_SELECT_DRINK("1301", "이미 음료를 선택했습니다."),
    EMPTY_MATCHING("1302", "해당되는 매칭이 없습니다."),
    NON_AUTHORIZATION_MATCHING("1303", "권한이 없는 매칭입니다."),
    ALREADY_MATCHING_REQUEST("1304", "이미 매칭 요청 중입니다."),
    LACK_OF_TICKET("1305", "매칭권이 부족합니다."),
    EMPTY_MATCHING_REQUEST("1306", "현재 요청하고 있는 매칭이 없습니다."),
    EMPTY_PARTNER_INFO("1307", "상대방을 조회하는 도중 에러가 발생했습니다."),
    REQUIRED_AVATAR("1308", "프로필을 공개하기 위해서는 프로필 이미지를 설정해야 합니다."),
    REQUIRED_ADDRESS("1309", "프로필을 공개하기 위해서는 주소를 설정해야 합니다."),
    EMPTY_REASON("1310", "유효하지 않은 이유입니다."),
    REQUIRED_REASON("1311", "사유를 입력해야 합니다."),
    INVALID_PROFILE_IMAGE_SEQUENCE("1312", "유효하지 않은 우선순위입니다."),
    NOT_YET_EXCHANGE_PROFILE("1313", "아직 프로필을 공개할 수 없습니다."),
    ALREADY_EXCHANGE_PROFILE("1314", "이미 프로필을 공개했습니다."),
    EXCEED_MATCHING_TOPIC("1315", "더 이상 존재하는 토픽이 없습니다."),
    EMPTY_TOPIC("1316", "현재 유효하지 않은 토픽입니다. "),

    // CHAT
    INVALID_MESSAGE_TYPE("1400", "메시지 타입이 올바르지 않습니다."),
    SEND_MESSAGE_ERROR("1401", "메시지 전송 도중 에러가 발생했습니다."),

    // FCM util
    FIREBASE_CREDENTIALS_ERROR("1500", "Firebase 인증 에러"),
    FIREBASE_INSERT_ERROR("1501", "Firebase 데이터 저장 에러" ),
    FIREBASE_BUILD_MESSAGE_ERROR("1502", "FCM 메시지 생성 중 발생했습니다."),
    FIREBASE_SEND_MESSAGE_ERROR("1503", "FCM 전송 중 에러가 발생했습니다."),

    // Mail Util
    EMAIL_SEND_ERROR("1600", "이메일 전송 중 에러가 발생했습니다."),

    BAD_REQUEST("4000", "잘못된 요청입니다."),
    FAILED_AUTHORIZATION("4001", "검증에 실패했습니다."),
    EXPIRED_TOKEN("4002", "토큰의 기한이 만료되었습니다."),
    FORBIDDEN_AUTHORIZATION("4003", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR("5000", "서버에 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
