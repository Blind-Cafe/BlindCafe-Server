package com.example.BlindCafe.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchingDetailDto {

    private Long matchingId;
    private String nickname;
    private String profileImage;
    private String drink;
    private String interest;
    private String startTime;
    private boolean isContinuous;
}
