package com.example.BlindCafe.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingStatus {

    REQUEST_SEARCH("요청 중"),
    REQUEST_WAIT("수락 대기 중"),

    SUCCESS_STAGE_ONE("3일간 대화"),
    SUCCESS_STAGE_TWO("7일간 대화"),

    FAILED_EXPIRED("7일 만료"),
    FAILED_OUT("방 나가기로 인한 폭파"),
    FAILED_REPORT("신고로 인한 폭파"),
    FAILED_EXCHANGE("프로필 교환 실패로 인한 폭파");

    private final String description;
}
