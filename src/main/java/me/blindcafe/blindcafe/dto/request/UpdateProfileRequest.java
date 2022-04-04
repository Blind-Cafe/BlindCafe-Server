package me.blindcafe.blindcafe.dto.request;

import me.blindcafe.blindcafe.domain.type.Gender;
import me.blindcafe.blindcafe.domain.type.Mbti;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotNull
    String state;

    @NotNull
    String region;

    @NotNull
    Gender partnerGender;

    @NotNull
    Mbti mbti;
}
