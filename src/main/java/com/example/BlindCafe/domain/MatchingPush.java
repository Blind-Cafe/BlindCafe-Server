package com.example.BlindCafe.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "matching_push")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingPush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_push_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    private boolean createMatching;
    private boolean oneDay;
    private boolean twoDays;
    private boolean endOfOneHour;
    private boolean threeDays;
    private boolean matchingContinue;
    private boolean lastChat;

    public static MatchingPush create() {
        MatchingPush push = new MatchingPush();
        push.setCreateMatching(false);
        push.setOneDay(false);
        push.setTwoDays(false);
        push.setEndOfOneHour(false);
        push.setThreeDays(false);
        push.setMatchingContinue(false);
        push.setLastChat(false);
        return push;
    }
}
