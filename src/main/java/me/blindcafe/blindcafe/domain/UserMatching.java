package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.domain.type.status.MatchingStatus;
import me.blindcafe.blindcafe.exception.BlindCafeException;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static me.blindcafe.blindcafe.exception.CodeAndMessage.*;

@Entity
@Table(name = "user_matching")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMatching extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_matching_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_id")
    private Drink drink;

    private String interests;

    private Boolean isAcceptExchangeProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status;

    public static UserMatching create(User user, List<Long> interest) {
        UserMatching userMatching = new UserMatching();
        // 티켓 소비
        user.consumeTicket();
        userMatching.setUser(user);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<interest.size(); i++) {
            sb.append(interest.get(i));
            if (i != interest.size()-1)
                sb.append(",");
        }
        userMatching.setDrink(null);
        userMatching.setInterests(sb.toString());
        userMatching.setIsAcceptExchangeProfile(null);
        userMatching.setStatus(MatchingStatus.WAIT);
        return userMatching;
    }

    // 매칭 성공
    public void success() {
        this.status = MatchingStatus.MATCHING;
    }

    // 매칭 취소
    public void cancel() {
        this.status = MatchingStatus.CANCEL;
        this.user.restoreTicket();
    }

    // 음료수 선택
    public void selectDrink(Drink drink) {
        // 이미 음료수를 고른 경우
        if (this.drink != null)
            throw new BlindCafeException(ALREADY_SELECT_DRINK);
        this.drink = drink;
    }

    // 프로필 교환하기
    public boolean exchangeProfile() {
        LocalDateTime now = LocalDateTime.now();

        // 프로필 교환 시점이 아닌 경우
        if (this.getMatching().getIsContinuous()
                || this.getMatching().getExpiredTime().isAfter(now))
            throw new BlindCafeException(NOT_YET_EXCHANGE_PROFILE);
        
        // 이미 프로필 교환 수락/거절한 경우
        if (this.isAcceptExchangeProfile != null)
            throw new BlindCafeException(ALREADY_EXCHANGE_PROFILE);

        // 프로필 교환 수락/거절
        this.isAcceptExchangeProfile = true;

        return this.getMatching().exchangeProfile();
    }
    
    // 방 나가기
    public void leave() {
        this.getMatching().inactive();
        this.getUser().getMatchings().remove(this);
        this.status = MatchingStatus.OUT;
    }
}
