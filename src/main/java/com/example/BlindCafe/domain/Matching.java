package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.status.MatchingStatus;
import com.example.BlindCafe.utils.DateTimeUtil;
import lombok.*;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "matching_push_id")
    private MatchingPush push;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "matching_topic_id")
    private MatchingTopic topic;

    private LocalDateTime beginTime;
    private LocalDateTime expiredTime;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isContinuous;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isOpenProfile;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isExchangeProfile;

    private boolean isActive;

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
            MatchingTopic topic,
            MatchingPush push
    ) {
        LocalDateTime now = LocalDateTime.now();
        Matching matching = new Matching();
        matching.setUserMatchings(userMatchings);
        matching.setInterest(interest);
        // 푸시
        matching.setPush(push);
        // 토픽 생성
        matching.setTopic(topic);
        // 시간 설정
        matching.setBeginTime(now);
        matching.setExpiredTime(now.plusDays(3));
        matching.setIsContinuous(false);
        matching.setIsOpenProfile(false);
        matching.setIsExchangeProfile(false);
        matching.setActive(true);
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

    // 다음 토픽 가져오기
    public Long getNextTopic() {
        return this.topic.getTopic();
    }
    
    // 특정 유저의 유저 매칭 가져오기
    public UserMatching getUserMatchingById(Long userId) {
        return this.userMatchings.stream()
                .filter(um -> um.getUser().getId().equals(userId))
                .findAny().orElse(null);
    }

    // 프로필 교환 확인
    public boolean exchangeProfile() {
        AtomicBoolean status = new AtomicBoolean(true);
        this.getUserMatchings().forEach(um -> {
            if (um.getIsAcceptExchangeProfile() == null || !um.getIsAcceptExchangeProfile())
                status.set(false);
        });
        this.isExchangeProfile = status.get();

        // 모두 프로필 교환 수락한 경우 7일 채팅으로 업데이트
        if (this.isExchangeProfile) {
            LocalDateTime now = LocalDateTime.now();
            this.isContinuous = true;
            this.beginTime = now;
            this.expiredTime = now.plusDays(7);

            // 사용자 음료수 뱃지 추가
            for (UserMatching um: this.getUserMatchings()) {
                User u = um.getUser();
                Drink d = um.getDrink();
                u.addDrink(d);
            }
        }
        return this.isExchangeProfile;
    }

    // 매칭에 속해 있는 사용자 ID 조회
    public List<String> getUserIds() {
        return this.getUserMatchings().stream()
                .map(UserMatching::getUser)
                .map(User::getId)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    // 채팅방 나가기
    public void leave(Long userId) {
        this.getUserMatchingById(userId).leave();
        // 채팅방 비활성화
        this.inactive();
    }

    // 채팅방 비활성화
    public void inactive() {
        this.isActive = false;
    }

    // 시간 별 채팅방 기능 허용 템플릿 전송
    public int sendMatchingFunction(LocalDateTime now) {
        if (this.isContinuous) return 0;
        if (!this.isActive) return 0;

        Long continuousTime = ChronoUnit.HOURS.between(this.getBeginTime(), now);
        if (continuousTime.equals(DateTimeUtil.HOUR_OF_ONE_DAY)) {
            if (this.getPush().isOneDay()) return 0;
            this.getPush().setOneDay(true);
            return 1;
        } else if (continuousTime.equals(DateTimeUtil.HOUR_OF_TWO_DAYS)) {
            if (this.getPush().isTwoDays()) return 0;
            this.getPush().setTwoDays(true);
            return 2;
        }
        return 0;
    }

    public boolean sendFirstTopic(LocalDateTime now) {
        if (!this.isActive) return false;
        if (this.isContinuous) return false;
        // 5분 지났는지 확인
        if (this.beginTime.plusMinutes(5L).isBefore(now)) return false;
        // 토픽 전송했는지 확인
        if (this.topic.getLatestTopic() != null) return false;
        return true;
    }

    // 3일 채팅에서 종료 1시간 전인지 확인 -> 마감 임박 메시지 전송
    public boolean sendEndOfBasicMatching(LocalDateTime now) {
        if (!this.isActive) return false;
        if (this.isContinuous) return false;
        if (this.getPush().isEndOfOneHour()) return false;

        Long continuousTime = ChronoUnit.HOURS.between(now, this.expiredTime);
        if (continuousTime.equals(1L)) {
            this.getPush().setEndOfOneHour(true);
            return true;
        }
        return false;
    }

    // 프로필 교환 템플릿 전송
    public boolean sendExchangeProfile(LocalDateTime now) {
        if (this.isContinuous) return false;
        if (this.expiredTime.isAfter(now)) return false;
        if (this.getPush().isThreeDays()) return false;
        if (!this.isActive) return false;

        this.getPush().setThreeDays(true);
        return true;
    }

    // 7일 채팅에서 종료까지 1일 남은 경우 종료 임박 템플릿 전송
    public boolean checkEndOfContinuousMatching(LocalDateTime now) {
        if(!this.isActive) return false;
        if (!this.isContinuous) return false;
        if (this.getPush().isLastChat()) return false;

        Long continuousTime = ChronoUnit.DAYS.between(now, this.expiredTime);
        if (continuousTime.equals(1L)) {
            this.getPush().setLastChat(true);
            return true;
        }
        return false;
    }

    // 7일 채팅 만료
    public void expiry(LocalDateTime now) {
        if (!this.isActive) return;
        if (!this.isContinuous) return;
        if (this.expiredTime.isAfter(now))
            this.isActive = false;
    }
}
