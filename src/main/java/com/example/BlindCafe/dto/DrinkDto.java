package com.example.BlindCafe.dto;

import com.example.BlindCafe.exception.CodeAndMessage;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class DrinkDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        private Long matchingId;
        @NotNull
        private Long drink;
    }

    @Getter
    @Setter
    public static class Response extends ApiResponse {
        private String startTime;

        @Builder
        public Response(CodeAndMessage codeAndMessage, String startTime) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.startTime = startTime;
        }
    }
}
