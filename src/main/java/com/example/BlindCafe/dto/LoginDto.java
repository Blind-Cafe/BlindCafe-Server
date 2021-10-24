package com.example.BlindCafe.dto;

import com.example.BlindCafe.type.AgeRange;
import com.example.BlindCafe.type.Gender;
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
    public static class KaKaoResponse {
        private String socialId;
        private Social socialType;
        private AgeRange ageRange;
        private Gender myGender;
    }
}
