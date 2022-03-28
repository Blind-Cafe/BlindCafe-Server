package com.example.BlindCafe.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Platform {
    AOS("AOS"),
    IOS("IOS");

    private final String description;
}
