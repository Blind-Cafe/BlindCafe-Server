package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.User;
import lombok.*;

import javax.validation.constraints.NotNull;

public class EditAddressDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private String state;
        @NotNull
        private String region;
    }

    @Getter
    @Builder
    public static class Response {
        private String region;

        public static Response fromEntity(User user) {
            String region = user.getAddress() != null ?
                    user.getAddress().toString() : null;
            return Response.builder()
                    .region(region)
                    .build();
        }
    }
}
