package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.Matching;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.type.Gender;
import lombok.*;

import javax.persistence.Enumerated;

import static javax.persistence.EnumType.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchingProfileDto {
    private String partnerNickname;
    private boolean isFill;
    private String profileImage;
    private String nickname;
    private String region;
    @Enumerated(STRING)
    private Gender gender;
    private int age;
}