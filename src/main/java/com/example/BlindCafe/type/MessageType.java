package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    TEXT(1),
    IMAGE(2),
    AUDIO(3),
    TEXT_TOPIC(4),
    IMAGE_TOPIC(5),
    AUDIO_TOPIC(6),
    DESCRIPTION(7);

    private final Integer firestoreType;
}
