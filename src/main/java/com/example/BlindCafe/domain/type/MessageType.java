package com.example.BlindCafe.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    TEXT(1, null, null, false),
    IMAGE(2, null, "사진이 전송되었습니다.", true),
    AUDIO(3, null, "소리가 전송되었습니다.", false),
    VIDEO(4, null, "영상이 전송되었습니다.", false),
    TEXT_TOPIC(5, null, "토픽이 전송되었습니다.", false),
    IMAGE_TOPIC(6, null, "토픽이 전송되었습니다.", false),
    AUDIO_TOPIC(7, null, "토픽이 전송되었습니다.", false),
    DESCRIPTION(8, null, null, false),
    DESCRIPTION_NON_PUSH(9, null, null, false),
    IS_EXCHANGE_PROFILE(10, "잠깐! 프로필 교환하실 시간입니다💝", "프로필을 교환하실 시간입니다", false),
    PROFILE(11, null, null, false);

    private final int type;
    private final String title;
    private final String body;
    private final boolean image;
}
