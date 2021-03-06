package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.domain.type.status.MatchingStatus;
import me.blindcafe.blindcafe.utils.DateTimeUtil;
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

    private boolean isContinuous;

    private boolean isOpenProfile;

    private boolean isExchangeProfile;

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
        // ??????
        matching.setPush(push);
        // ?????? ??????
        matching.setTopic(topic);
        // ?????? ??????
        matching.setBeginTime(now);
        matching.setExpiredTime(now.plusDays(3));
        matching.setContinuous(false);
        matching.setOpenProfile(false);
        matching.setExchangeProfile(false);
        matching.setActive(true);
        matching.setStatus(MatchingStatus.MATCHING);

        // ?????? ???????????? ????????????
        matching.updateMatchingHistory();

        return matching;
    }
    
    // ?????? ???????????? ????????????
    public void updateMatchingHistory() {
        List<Long> users = this.getUserMatchings().stream()
                .map(UserMatching::getUser)
                .map(User::getId)
                .collect(Collectors.toList());

        for (int i=0; i<2; i++) {
            this.getUserMatchings().get(i).getUser().updateMatchingHistory(users.get(1-i));
        }
    }

    // ?????? ?????? ????????????
    public Long getNextTopic() {
        return this.topic.getTopic();
    }
    
    // ?????? ????????? ?????? ?????? ????????????
    public UserMatching getUserMatchingById(Long userId) {
        return this.userMatchings.stream()
                .filter(um -> um.getUser().getId().equals(userId))
                .findAny().orElse(null);
    }

    // ????????? ?????? ??????
    public boolean exchangeProfile() {
        AtomicBoolean exchangeStatus = new AtomicBoolean(true);
        this.getUserMatchings().forEach(um -> {
            if (um.getIsAcceptExchangeProfile() == null || !um.getIsAcceptExchangeProfile())
                exchangeStatus.set(false);
        });
        this.isExchangeProfile = exchangeStatus.get();

        // ?????? ????????? ?????? ????????? ?????? 7??? ???????????? ????????????
        if (Boolean.TRUE.equals(this.isExchangeProfile)) {
            LocalDateTime now = LocalDateTime.now();
            this.isContinuous = true;
            this.beginTime = now;
            this.expiredTime = now.plusDays(7);

            // ????????? ????????? ?????? ??????
            for (UserMatching um: this.getUserMatchings()) {
                User u = um.getUser();
                Drink d = um.getDrink();
                u.addDrink(d);
            }
        }
        return this.isExchangeProfile;
    }

    // ????????? ?????? ?????? ????????? ID ??????
    public List<String> getUserIds() {
        return this.getUserMatchings().stream()
                .map(UserMatching::getUser)
                .map(User::getId)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    // ????????? ?????????
    public void leave(Long userId) {
        this.getUserMatchingById(userId).leave();
        // ????????? ????????????
        this.inactive();
    }

    // ????????? ????????????
    public void inactive() {
        this.isActive = false;
    }

    // ?????? ??? ????????? ?????? ?????? ????????? ??????
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
        // 5??? ???????????? ??????
        if (this.beginTime.plusMinutes(5L).isBefore(now)) return false;
        // ?????? ??????????????? ??????
        return this.topic.getLatestTopic() == null;
    }

    // 3??? ???????????? ?????? 1?????? ????????? ?????? -> ?????? ?????? ????????? ??????
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

    // ????????? ?????? ????????? ??????
    public boolean sendExchangeProfile(LocalDateTime now) {
        if (this.isContinuous) return false;
        if (this.expiredTime.isAfter(now)) return false;
        if (this.getPush().isThreeDays()) return false;
        if (!this.isActive) return false;

        this.getPush().setThreeDays(true);
        return true;
    }

    // 7??? ???????????? ???????????? 1??? ?????? ?????? ?????? ?????? ????????? ??????
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

    // 7??? ?????? ??????
    public void expiry(LocalDateTime now) {
        if (!this.isActive) return;
        if (!this.isContinuous) return;
        if (this.expiredTime.isAfter(now))
            this.isActive = false;
    }
}
