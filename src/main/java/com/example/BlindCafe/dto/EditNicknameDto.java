package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.User;
import lombok.*;

import javax.validation.constraints.Size;

public class EditNicknameDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @Size(min = 1, max = 10, message = "nickname min 1 max 10")
        private String nickname;
    }

    @Getter
    @Builder
    public static class Response {
        private String nickname;

        public static Response fromEntity(User user) {
            return Response.builder()
                    .nickname(user.getNickname())
                    .build();
        }
    }
}
