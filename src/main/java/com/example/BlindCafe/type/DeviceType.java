package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceType {
    AOS("안드로이드"),
    IOS("애플");

    private final String description;
}
