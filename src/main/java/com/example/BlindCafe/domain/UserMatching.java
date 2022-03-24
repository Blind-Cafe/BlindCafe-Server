package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.status.MatchingStatus;
import lombok.*;

import javax.persistence.*;
import java.util.List;

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
}
