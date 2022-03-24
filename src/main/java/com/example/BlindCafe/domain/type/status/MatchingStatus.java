package com.example.BlindCafe.domain.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingStatus {

    WAIT("매칭 대기 중"),
    CANCEL("요청 취소"),
    MATCHING("매칭 성공"),
    OUT("방 나가기");

    private final String description;
}
