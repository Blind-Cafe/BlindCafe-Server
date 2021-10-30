package com.example.BlindCafe.dto;

import lombok.*;

@Getter
@Setter
public class ErrorResponse extends ApiResponse {

    @Builder
    public ErrorResponse(String code, String message) {
        super(code, message);
    }
}
