package com.example.BlindCafe.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingStatus {

    NONE("매칭 없음"),
    WAIT("매칭 대기 중"),
    MATCHING("3일간 대화"),
    MATCHING_CONTINUE("7일간 대화"),

    FAILED_EXPIRED("7일 만료"),
    FAILED_OUT("방 나가기로 인한 폭파"),
    FAILED_REPORT("신고로 인한 폭파"),
    FAILED_EXCHANGE("프로필 교환 실패로 인한 폭파");

    private final String description;
}
