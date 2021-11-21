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

    public MatchingDetailDto(Matching matching, User user) {
        ProfileImage profileImage = user.getProfileImages()
                .stream().sorted(Comparator.comparing(ProfileImage::getPriority))
                .filter(pi -> pi.getStatus().equals(NORMAL))
                .findFirst()
                .orElse(null);
        String src = profileImage != null ? profileImage.getSrc() : null;

        Drink drink = user.getUserMatchings().stream()
                .filter(um -> um.getMatching().equals(matching))
                .findAny()
                .map(um -> um.getDrink())
                .orElse(null);


        LocalDateTime ldt = matching.getStartTime();
        Timestamp timestamp = Timestamp.valueOf(ldt);
        String startTime = String.valueOf(timestamp.getTime() / 1000);

        this.matchingId = matching.getId();
        this.profileImage = src;
        this.nickname = user.getNickname();
        this.drink = drink == null ? "미입력" : drink.getName();
        this.startTime = startTime;
        this.interest = matching.getInterest().getName();
    }
}
