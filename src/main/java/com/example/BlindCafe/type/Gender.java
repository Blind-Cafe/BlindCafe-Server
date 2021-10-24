package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {

    M("남성"),
    F("여성"),
    N("상관없음");

    private final String description;
}
