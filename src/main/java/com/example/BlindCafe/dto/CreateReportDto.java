package com.example.BlindCafe.dto;

import com.example.BlindCafe.dto.response.ApiResponse;
import com.example.BlindCafe.exception.CodeAndMessage;
import lombok.*;

import javax.validation.constraints.NotNull;

public class CreateReportDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        private Long matchingId;
        @NotNull
        private Long reason;
    }

    @Getter
    @Setter
    public static class Response extends ApiResponse {
        @Builder
        public Response(CodeAndMessage codeAndMessage) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
        }
    }
}
