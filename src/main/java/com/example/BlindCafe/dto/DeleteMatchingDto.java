package com.example.BlindCafe.dto;

import com.example.BlindCafe.dto.response.ApiResponse;
import com.example.BlindCafe.exception.CodeAndMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteMatchingDto extends ApiResponse {
    @Builder
    public DeleteMatchingDto(CodeAndMessage codeAndMessage) {
        super(codeAndMessage.getCode(), codeAndMessage.getMessage());
    }
}
