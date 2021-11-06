package com.example.BlindCafe.dto;

import com.example.BlindCafe.exception.CodeAndMessage;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;

public class EditInterestDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        @Size(min = 3, max = 3, message = "interest length 3")
        private ArrayList<CreateUserInfoDto.Interest> interests;
    }

    public static class Response extends ApiResponse {
        @Builder
        public Response(CodeAndMessage codeAndMessage) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
        }
    }
}
