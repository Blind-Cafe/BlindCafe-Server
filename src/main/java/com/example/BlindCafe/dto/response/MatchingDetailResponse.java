package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.Drink;
import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.UserMatching;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingDetailResponse {

    private Long matchingId;
    private Partner partner;
    private boolean isSelectedDrink;
    private LocalDateTime beginDt;
    private LocalDateTime expiredDt;
    private boolean isContinuous;

    public static MatchingDetailResponse fromEntity(Matching matching, Long userId) {
        User partner = matching.getUserMatchings().stream()
                .filter(userMatching -> !userMatching.getUser().getId().equals(userId))
                .findAny()
                .map(partnerMatching -> partnerMatching.getUser()).orElse(null);

        MatchingDetailResponse response = new MatchingDetailResponse();
        response.setMatchingId(matching.getId());
        response.setPartner(Partner.fromEntity(partner));

        boolean isSelectedDrink = false;
        Drink selectedDrink = matching.getUserMatchings().stream()
                .filter(userMatching -> userMatching.getUser().getId().equals(userId))
                .findAny()
                .map(UserMatching::getDrink).orElse(null);
        if (selectedDrink != null)
            isSelectedDrink = true;
        response.setSelectedDrink(isSelectedDrink);
        response.setBeginDt(matching.getBeginTime());
        response.setExpiredDt(matching.getExpiredTime());
        response.setContinuous(matching.getIsContinuous());
        return response;
    }
}
