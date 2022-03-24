package com.example.BlindCafe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeProfileRequest {
    @NotNull
    private Long matchingId;
    @NotNull
    private boolean accept;

    private Long reason;
}
