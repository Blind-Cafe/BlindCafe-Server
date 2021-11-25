package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;

import static com.example.BlindCafe.type.status.CommonStatus.NORMAL;

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
}
