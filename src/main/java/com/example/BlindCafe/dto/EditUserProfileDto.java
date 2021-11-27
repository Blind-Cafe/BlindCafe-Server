package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.ProfileImage;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.type.Gender;
import com.example.BlindCafe.type.status.CommonStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

import static javax.persistence.EnumType.STRING;

public class EditUserProfileDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @NotNull
        @Size(min = 1, max = 10, message = "name min 1 max 10")
        String nickname;
        @NotNull
        Gender partnerGender;
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
        private int age;
        @Enumerated(STRING)
        private Gender myGender;
        @Enumerated(STRING)
        private Gender partnerGender;
        private String region;

        public static Response fromEntity(User user) {
            String region = Objects.isNull(user.getAddress()) ? null : user.getAddress().toString();
            return Response.builder()
                    .nickname(user.getNickname())
                    .age(user.getAge())
                    .myGender(user.getMyGender())
                    .partnerGender(user.getPartnerGender())
                    .region(region)
                    .build();
        }
    }
}
