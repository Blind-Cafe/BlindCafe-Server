package me.blindcafe.blindcafe.dto.request;

import me.blindcafe.blindcafe.domain.type.Platform;
import me.blindcafe.blindcafe.domain.type.Social;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotNull
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Social social;

    @NotNull
    private String accessToken;

    @NotNull
    private String deviceToken;
}
