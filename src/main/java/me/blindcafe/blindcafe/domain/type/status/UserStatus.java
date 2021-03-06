package me.blindcafe.blindcafe.domain.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatus {
    NOT_YET("필수 정보 미입력"),
    NORMAL("일반"),
    SUSPENDED("제재"),
    RETIRED("탈퇴");

    private final String description;
}
