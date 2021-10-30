package com.example.BlindCafe.dto;

import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.type.Social;
import lombok.*;

import javax.validation.constraints.NotNull;

public class LoginDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        private String token;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SocialResponse {
        @NotNull
        private String socialId;
        @NotNull
        private Social socialType;
    }

    @Getter
    @Setter
    public static class Response extends ApiResponse {
        private String jwt;

        @Builder
        public Response(CodeAndMessage codeAndMessage, String jwt) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.jwt = jwt;
        }
    }
}
