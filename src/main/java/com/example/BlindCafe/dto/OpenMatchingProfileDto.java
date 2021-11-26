package com.example.BlindCafe.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenMatchingProfileDto {
    private String nickname;
    private boolean result;
}
