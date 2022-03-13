package com.example.BlindCafe.dto.request;

import com.example.BlindCafe.entity.type.Platform;
import com.example.BlindCafe.entity.type.Social;
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
