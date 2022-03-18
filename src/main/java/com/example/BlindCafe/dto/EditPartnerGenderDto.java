package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.type.Gender;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditPartnerGenderDto {
    @NotNull
    private Gender gender;
}
