package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.status.MatchingStatus;
import com.example.BlindCafe.exception.BlindCafeException;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

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

    private Boolean isAcceptOpenProfile;

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
        userMatching.setIsAcceptOpenProfile(null);
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
        this.status = MatchingStatus.CANCEL_REQUEST;
        this.user.restoreTicket();
    }

    // 음료수 선택
    public void selectDrink(Drink drink) {
        this.drink = drink;
    }
    
    // 프로필 공개하기
    public boolean openProfile(boolean isAccept) {
        LocalDateTime now = LocalDateTime.now();

        // 프로필 공개 시점이 아닌 경우
        if (this.getMatching().getIsContinuous()
                || this.getMatching().getExpiredTime().isAfter(now))
            throw new BlindCafeException(NOTYET_OPEN_PROFILE);

        // 이미 프로필 공개를 수락/거절한 경우
        if (this.isAcceptOpenProfile != null)
            throw new BlindCafeException(ALREADY_OPEN_PROFILE);

        // 프로필 공개 수락/거절
        this.isAcceptOpenProfile = isAccept;

        // 프로필 공개 거절한 경우 채팅방 비활성화
        if (isAccept == false)
            this.getMatching().inactive();

        return this.getMatching().openProfile();
    }

    // 프로필 교환하기
    public boolean exchangeProfile(boolean isAccept) {

        // 프로필 교환 시점이 아닌 경우
        if (this.isAcceptOpenProfile != true)
            throw new BlindCafeException(NOTYET_EXCHANGE_PROFILE);
        
        // 이미 프로필 교환 수락/거절한 경우
        if (this.isAcceptExchangeProfile != null)
            throw new BlindCafeException(ALREADY_EXCHANGE_PROFILE);

        // 프로필 교환 수락/거절
        this.isAcceptExchangeProfile = isAccept;

        // 프로필 교환 거절한 경우 채팅방 비활성화
        if (isAccept == false)
            this.getMatching().inactive();

        return this.getMatching().exchangeProfile();
    }
}
