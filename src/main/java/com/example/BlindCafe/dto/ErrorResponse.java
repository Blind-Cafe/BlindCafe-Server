package com.example.BlindCafe.dto;

import com.example.BlindCafe.exception.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private String code;
    private String message;
}
