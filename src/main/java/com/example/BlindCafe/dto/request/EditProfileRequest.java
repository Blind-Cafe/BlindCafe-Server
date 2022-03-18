package com.example.BlindCafe.dto.request;

import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.domain.type.Mbti;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class EditProfileRequest {

    @NotNull
    String state;

    @NotNull
    String region;

    @NotNull
    Gender partnerGender;

    @NotNull
    Mbti mbti;
}
