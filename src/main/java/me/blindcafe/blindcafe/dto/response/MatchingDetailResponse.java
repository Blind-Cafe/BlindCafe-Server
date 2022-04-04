package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.Drink;
import me.blindcafe.blindcafe.domain.Matching;
import me.blindcafe.blindcafe.domain.User;
import me.blindcafe.blindcafe.domain.UserMatching;
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
    private PartnerResponse partner;
    private boolean isSelectedDrink;
    private LocalDateTime beginDt;
    private LocalDateTime expiredDt;
    private TopicResponse topic;
    private boolean isContinuous;
    private boolean isActive;

    public static MatchingDetailResponse fromEntity(Matching matching, Long userId, TopicResponse topic) {
        User partner = matching.getUserMatchings().stream()
                .filter(userMatching -> !userMatching.getUser().getId().equals(userId))
                .findAny()
                .map(UserMatching::getUser).orElse(null);

        MatchingDetailResponse response = new MatchingDetailResponse();
        response.setMatchingId(matching.getId());
        response.setPartner(PartnerResponse.fromEntity(partner));

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
        response.setTopic(topic);
        response.setContinuous(matching.getIsContinuous());
        response.setActive(matching.isActive());
        return response;
    }
}
