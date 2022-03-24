package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.status.MatchingStatus;
import lombok.*;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Entity
@Table(name = "matching")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    @OneToMany(mappedBy = "matching", cascade = CascadeType.ALL)
    private List<UserMatching> userMatchings = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_push_id")
    private MatchingPush push;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_topic_id")
    private MatchingTopic topic;

    private LocalDateTime beginTime;
    private LocalDateTime expiredTime;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isContinuous;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isExchangeProfile;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'MATCHING'", nullable = false)
    private MatchingStatus status;

    public void setUserMatchings(List<UserMatching> userMatchings) {
        userMatchings.forEach(um -> {
            um.success();
            um.setMatching(this);
        });
        this.userMatchings = userMatchings;
    }

    public void setPush(MatchingPush push) {
        this.push = push;
        push.setMatching(this);
    }

    public void setTopic(MatchingTopic topic) {
        this.topic = topic;
        topic.setMatching(this);
    }

    public static Matching create(
            List<UserMatching> userMatchings,
            Interest interest,
            MatchingTopic topic
    ) {
        LocalDateTime now = LocalDateTime.now();
        Matching matching = new Matching();
        matching.setUserMatchings(userMatchings);
        matching.setInterest(interest);
        // 푸시
        MatchingPush push = new MatchingPush();
        matching.setPush(push);
        // 토픽 생성
        matching.setTopic(topic);
        // 시간 설정
        matching.setBeginTime(now);
        matching.setExpiredTime(now.plusDays(3));
        matching.setIsContinuous(false);
        matching.setIsExchangeProfile(false);
        matching.setStatus(MatchingStatus.MATCHING);

        // 매칭 히스토리 업데이트
        matching.updateMatchingHistory();

        return matching;
    }
    
    // 매칭 히스토리 업데이트
    public void updateMatchingHistory() {
        List<Long> users = this.getUserMatchings().stream()
                .map(UserMatching::getUser)
                .map(User::getId)
                .collect(Collectors.toList());

        for (int i=0; i<2; i++) {
            this.getUserMatchings().get(i).getUser().updateMatchingHistory(users.get(1-i));
        }
    }

    // 토픽 가져오기
    public Long getTopic() {
        return this.topic.getTopic();
    }
    
    // 특정 유저의 유저 매칭 가져오기
    public UserMatching getUserMatchingById(Long userId) {
        return this.userMatchings.stream()
                .filter(um -> um.getUser().getId().equals(userId))
                .findAny().orElse(null);
    }

    // 프로필 교환 확인
    public boolean checkExchangeProfile() {
        AtomicBoolean status = new AtomicBoolean(true);
        this.getUserMatchings().forEach(um -> {
            if (!um.isProfileOpen())
                status.set(false);
        });
        this.isExchangeProfile = status.get();

        // 모두 프로필 교환 수락한 경우 7일 채팅으로 업데이트
        if (this.isExchangeProfile) {
            LocalDateTime now = LocalDateTime.now();
            this.isContinuous = true;
            this.beginTime = now;
            this.expiredTime = now.plusDays(7);
        }

        return this.isExchangeProfile;
    }
}
