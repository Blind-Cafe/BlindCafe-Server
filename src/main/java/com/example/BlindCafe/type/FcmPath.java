package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmPath {
    HOME("홈"),
    CHAT("채팅방");

    private final String description;
}
