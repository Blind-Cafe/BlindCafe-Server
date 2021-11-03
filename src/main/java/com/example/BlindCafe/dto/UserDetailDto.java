package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.type.Gender;
import lombok.*;

import javax.persistence.Enumerated;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.type.status.CommonStatus.SELECTED;
import static javax.persistence.EnumType.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailDto {
    private String profileImage;
    private String nickname;
    @Enumerated(STRING)
    private Gender myGender;
    private int age;
    private String region;
    private List<Long> interests;
    private List<Long> badges;

    public static UserDetailDto fromEntity(User user) {
        ProfileImage profileImage = user.getProfileImages()
                .stream().filter(pi -> pi.getStatus().equals(SELECTED))
                .findAny()
                .orElse(null);
        String src = profileImage != null ?
                profileImage.getSrc() : null;
        String region = user.getAddress() != null ?
                user.getAddress().toString() : null;

        return UserDetailDto.builder()
                .profileImage(src)
                .nickname(user.getNickname())
                .myGender(user.getMyGender())
                .age(user.getAge())
                .region(region)
                .interests(
                        user.getInterestOrders()
                        .stream().map(InterestOrder::getInterest)
                        .map(Interest::getId).collect(Collectors.toList())
                )
                .badges(
                        user.getUserBadges()
                        .stream().map(UserBadge::getId)
                        .collect(Collectors.toList())
                )
                .build();
    }
}
