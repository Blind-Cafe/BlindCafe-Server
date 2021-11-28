package com.example.BlindCafe.dto;

import com.example.BlindCafe.type.Gender;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class OpenMatchingProfileDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull
        @Size(min = 1, max = 10, message = "name min 1 max 10")
        String nickname;
        String state;
        String region;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {
        private String nickname;
        private boolean result;
    }
}
