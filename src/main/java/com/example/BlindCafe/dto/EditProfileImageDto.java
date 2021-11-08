package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.ProfileImage;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.entity.UserMatching;
import com.example.BlindCafe.type.status.CommonStatus;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.type.status.CommonStatus.NORMAL;
import static java.util.Comparator.comparing;

public class EditProfileImageDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {
        @NotNull
        @Min(1)
        @Max(3)
        private int priority;

        @NotNull
        private String src;
    }

    @Getter
    @Builder
    public static class Response {
        private List<ProfileImageDto> profileImages;

        public static Response fromEntity(User user) {
            return Response.builder()
                    .profileImages(user.getProfileImages()
                            .stream()
                            .sorted(comparing(ProfileImage::getPriority))
                            .filter(profileImage -> profileImage.getStatus().equals(NORMAL))
                            .map(ProfileImageDto::new)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    public static class ProfileImageDto {
        private int priority;
        private String src;

        public ProfileImageDto (ProfileImage profileImage) {
            this.priority = profileImage.getPriority();
            this.src = profileImage.getSrc();
        }
    }
}
