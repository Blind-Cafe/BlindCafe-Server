package com.example.BlindCafe.type.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportStatus {

    WAIT("처리 중"),
    COMPLETED("처리 완료");

    private final String description;
}
