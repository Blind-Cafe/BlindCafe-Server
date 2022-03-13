package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.entity.type.Gender;
import lombok.*;

import javax.persistence.Enumerated;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.entity.type.status.CommonStatus.NORMAL;
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
    @Enumerated(STRING)
    private Gender partnerGender;
    private List<Long> interests;
    private List<Long> drinks;

    public static UserDetailDto fromEntity(User user) {
        ProfileImage profileImage = user.getProfileImages()
                .stream().sorted(Comparator.comparing(ProfileImage::getPriority))
                .filter(pi -> pi.getStatus().equals(NORMAL))
                .findFirst()
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
                .partnerGender(user.getPartnerGender())
                .interests(
                        user.getInterestOrders()
                        .stream().map(InterestOrder::getInterest)
                        .map(Interest::getId).collect(Collectors.toList())
                )
                .drinks(
                        user.getUserDrinks().stream()
                                .map(UserDrink::getDrink)
                                .map(Drink::getId)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
