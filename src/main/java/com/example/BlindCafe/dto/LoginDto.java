package com.example.BlindCafe.dto;

import com.example.BlindCafe.dto.response.ApiResponse;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.entity.type.Platform;
import com.example.BlindCafe.entity.type.Social;
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
        @NotNull
        private String deviceId;
        private Platform platform;
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
        private Long id;
        private String nickname;

        @Builder
        public Response(CodeAndMessage codeAndMessage, String jwt, Long id, String nickname) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.jwt = jwt;
            this.id = id;
            this.nickname = nickname;
        }
    }
}
