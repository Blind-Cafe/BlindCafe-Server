package com.example.BlindCafe.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingValid {

    VALID("유효"),
    NON_VALID("유효하지 않음");

    private final String description;
}
