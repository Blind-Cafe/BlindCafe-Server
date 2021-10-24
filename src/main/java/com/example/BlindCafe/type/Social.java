package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Social {
    KAKAO("카카오"),
    APPLE("애플");

    private final String description;
}
