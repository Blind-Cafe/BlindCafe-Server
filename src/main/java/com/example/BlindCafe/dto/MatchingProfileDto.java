package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.type.Gender;
import lombok.*;

import javax.persistence.Enumerated;

import java.util.List;

import static javax.persistence.EnumType.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchingProfileDto {
    private boolean fill;
    private Long userId;
    private String partnerNickname;
    private String profileImage;
    private String nickname;
    private String region;
    @Enumerated(STRING)
    private Gender gender;
    private List<String> interests;
    private int age;
}
