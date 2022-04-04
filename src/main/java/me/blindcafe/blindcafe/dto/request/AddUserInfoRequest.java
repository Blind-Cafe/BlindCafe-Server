package me.blindcafe.blindcafe.dto.request;

import me.blindcafe.blindcafe.domain.type.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddUserInfoRequest {
    @NotNull
    @Min(18)
    private int age;

    @NotNull
    private Gender myGender;

    @NotNull
    private String phone;

    @NotNull
    @Size(min = 1, max = 10, message = "name min 1 max 10")
    private String nickname;

    @NotNull
    private Gender partnerGender;

    @NotNull
    @Size(min = 3, max = 3, message = "interest length 3")
    private List<Long> interests;
}
