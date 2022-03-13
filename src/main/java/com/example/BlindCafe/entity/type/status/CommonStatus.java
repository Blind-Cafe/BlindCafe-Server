package com.example.BlindCafe.entity.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonStatus {

    SELECTED("사용"),
    NORMAL("일반"),
    DELETED("삭제");

    private final String description;
}
