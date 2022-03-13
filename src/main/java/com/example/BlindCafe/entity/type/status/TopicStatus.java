package com.example.BlindCafe.entity.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopicStatus {

    WAIT("대기"),
    SELECT("선택"),
    REJECT("거절");

    private final String description;
}
