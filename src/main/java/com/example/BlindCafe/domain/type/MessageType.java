package com.example.BlindCafe.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    TEXT(1, ""),
    IMAGE(2, "사진이 전송되었습니다."),
    AUDIO(3, "소리가 전송되었습니다."),
    TEXT_TOPIC(4, "토픽이 전송되었습니다."),
    IMAGE_TOPIC(5, "토픽이 전송되었습니다."),
    AUDIO_TOPIC(6, "토픽이 전송되었습니다."),
    DESCRIPTION(7, ""),
    DRINK(8, "음료수가 선택되었습니다.");

    private final int type;
    private final String msg;
}
