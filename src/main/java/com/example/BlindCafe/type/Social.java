package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Social {
    KAKAO("KAKAO", "카카오"),
    APPLE("APPLE", "애플");

    private final String eng;
    private final String kor;
}
