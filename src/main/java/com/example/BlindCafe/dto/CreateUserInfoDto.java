package com.example.BlindCafe.dto;

import com.example.BlindCafe.dto.ApiResponse;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.type.Gender;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;

public class CreateUserInfoDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        @Min(18)
        private int age;

        @NotNull
        private Gender myGender;

        @NotNull
        @Size(min = 1, max = 10, message = "name min 1 max 10")
        private String nickname;

        @NotNull
        private Gender partnerGender;

        @NotNull
        @Size(min = 3, max = 3, message = "interest length 3")
        private ArrayList<Interest> interests;
    }

    @Getter
    @Setter
    public static class Interest {
        @NotNull
        private Long main;
        @NotNull
        private ArrayList<String> sub;
    }

    public static class Response extends ApiResponse {
        @Builder
        public Response(CodeAndMessage codeAndMessage) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
        }
    }
}
