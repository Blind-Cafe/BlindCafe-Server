package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.*;
import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.domain.type.Mbti;
import lombok.*;

import javax.persistence.Enumerated;

import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.EnumType.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailResponse {

    private String avatar;
    private String nickname;
    private String phone;
    @Enumerated(STRING)
    private Gender myGender;
    @Enumerated(STRING)
    private Gender partnerGender;
    private int age;
    private String address;
    @Enumerated(STRING)
    private Mbti mbti;
    private String voice;
    private List<Long> interests;
    private List<Long> drinks;

    public static UserDetailResponse fromEntity(User user) {
        return UserDetailResponse.builder()
                .avatar(user.getMainAvatar())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .myGender(user.getMyGender())
                .partnerGender(user.getPartnerGender())
                .age(user.getAge())
                .address(user.getAddress())
                .mbti(user.getMbti())
                .voice(user.getVoice())
                .interests(
                        user.getInterests().stream()
                                .map(UserInterest::getInterest)
                                .map(Interest::getId)
                                .collect(Collectors.toList()))
                .drinks(
                        user.getDrinks().stream()
                                .map(UserDrink::getDrink)
                                .map(Drink::getId).collect(Collectors.toList()))
                .build();
    }
}
