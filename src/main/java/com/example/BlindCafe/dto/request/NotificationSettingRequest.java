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
public class NotificationSettingRequest {
    @NotNull
    private Long target;
    @NotNull
    private boolean activate;
}
