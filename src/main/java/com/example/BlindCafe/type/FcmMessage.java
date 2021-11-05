package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmMessage {
    MATCHING("매칭 성공", "상대방이 매칭되었습니다.", "home");

    private final String title;
    private final String body;
    private final String path;
}
