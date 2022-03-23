package com.example.BlindCafe.domain.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingStatus {

    WAIT("매칭 대기 중"),
    CANCEL_REQUEST("요청 취소"),

    FOUND("대화 상대 찾음, 음료수 미선택"),
    MATCHING_NOT_START("음료수 미선택인 매칭"),
    MATCHING("3일간 대화"),
    PROFILE_EXCHANGE("프로필 교환"),

    PROFILE_OPEN("프로필 공개 여부 선택"),
    PROFILE_READY("프로필 공개"),
    PROFILE_ACCEPT("프로필 교환 수락"),
    MATCHING_CONTINUE_YET("프로필 교환 성공 후 대기"),

    CANCEL_REQUEST_EXPIRED("시간 초과로 인해 요청 취소"),

    CANCEL_EXPIRED("24시간 내 음료수 미선택"),

    MATCHING_CONTINUE("7일간 대화"),
    FAILED_EXPIRED("7일 만료"),

    OUT("방 나가기"),
    FAILED_LEAVE_ROOM("방 나가기로 인한 폭파"),
    FAILED_REPORT("신고로 인한 폭파"),
    FAILED_WONT_EXCHANGE("프로필 교환 거절로 인한 폭파"),
    FAILED_INVALID_USER("상대방의 상태가 유효하지 않습니다.");

    private final String description;
}
