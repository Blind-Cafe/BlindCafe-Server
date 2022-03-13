package com.example.BlindCafe.dto;

import com.example.BlindCafe.dto.response.ApiResponse;
import com.example.BlindCafe.exception.CodeAndMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class DeleteUserDto {

    @Getter
    @Setter
    public static class Response extends ApiResponse {
        private String nickname;

        @Builder
        public Response(CodeAndMessage codeAndMessage, String nickname) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.nickname = nickname;
        }
    }
}
