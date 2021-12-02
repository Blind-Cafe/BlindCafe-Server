package com.example.BlindCafe.util;

import com.example.BlindCafe.entity.Matching;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.entity.UserMatching;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import com.example.BlindCafe.repository.MatchingRepository;
import com.example.BlindCafe.type.FcmMessage;
import com.example.BlindCafe.type.status.MatchingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Scheduler {

    private final static int DELAY_TEN_MIN = 1000 * 60 * 10;
    private final static int DELAY_ONE_HOUR = 1000 * 60 * 60;
    private final static int DELAY_ONE_DAY = 1000 * 60 * 60 * 24;

    private final static Long ONE_DAY = 24L;
    private final static Long TWO_DAYS = 48L;

    private final MatchingRepository matchingRepository;
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    // task 1, 2
    @Scheduled(fixedDelay = DELAY_TEN_MIN)
    @Transactional
    public void updateTenMin() {
        LocalDateTime now = LocalDateTime.now();
        List<Matching> matchings = matchingRepository.findAll();
        checkBasicMatching(matchings, now);
        checkContinuousMatching(matchings, now);
    }

    // task 3, 4, 5
    @Scheduled(fixedDelay = DELAY_ONE_HOUR)
    @Transactional
    public void updateOneHour() {
        LocalDateTime now = LocalDateTime.now();
        List<Matching> matchings = matchingRepository.findAll();
        checkDayFunction(matchings, now, ONE_DAY);
        checkDayFunction(matchings, now, TWO_DAYS);
        sendProfile(matchings, now);
    }

    // task 6
    @Scheduled(fixedDelay = DELAY_ONE_DAY)
    @Transactional
    public void updateOneDay() {
        LocalDateTime now = LocalDateTime.now();
        List<Matching> matchings = matchingRepository.findAll();
        checkEndOfMatching(matchings, now);
    }

    /**
     *  1. 3일 끝났는지 확인해서 상태 변경
     */
    private void checkBasicMatching(List<Matching> matchings, LocalDateTime time) {
        matchings = matchings.stream()
                .filter(matching -> matching.getStatus().equals(MatchingStatus.MATCHING))
                .filter(matching -> matching.getExpiryTime().isBefore(time))
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            matching.setStatus(MatchingStatus.PROFILE_EXCHANGE);
            matching.setIsContinuous(true);
            List<UserMatching> userMatchings = matching.getUserMatchings();
            for (UserMatching userMatching: userMatchings) {
                userMatching.setStatus(MatchingStatus.PROFILE_OPEN);
            }
        }
    }

    /**
     *  2. 7일 끝났는지 확인해서 상태 변경
     */
    private void checkContinuousMatching(List<Matching> matchings, LocalDateTime time) {
        matchings = matchings.stream()
                .filter(matching -> matching.getStatus().equals(MatchingStatus.MATCHING_CONTINUE))
                .filter(matching -> matching.getExpiryTime().isBefore(time))
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            matching.setStatus(MatchingStatus.FAILED_EXPIRED);
            List<UserMatching> userMatchings = matching.getUserMatchings();
            for (UserMatching userMatching: userMatchings) {
                userMatching.setStatus(MatchingStatus.FAILED_EXPIRED);
            }
        }
    }

    /**
     *  3. 24시간 푸쉬 , 4. 48시간 푸쉬
     */
    private void checkDayFunction(List<Matching> matchings, LocalDateTime time, Long hour) {
        matchings = matchings.stream()
                .filter(matching -> matching.getStatus().equals(MatchingStatus.MATCHING))
                .filter(matching -> ChronoUnit.HOURS.between(matching.getStartTime(), time) == hour)
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            if (hour == ONE_DAY)
                sendPushMessage(matching, FcmMessage.ONE_DAY, null);
            if (hour == TWO_DAYS)
                sendPushMessage(matching, FcmMessage.TWO_DAYS, null);
        }
    }

    /**
     *  5. 7시간 뒤 프로필 자동 전송
     */
    private void sendProfile(List<Matching> matchings, LocalDateTime time) {
        matchings = matchings.stream()
                .filter(matching -> matching.getStatus().equals(MatchingStatus.PROFILE_EXCHANGE))
                .filter(matching -> ChronoUnit.HOURS.between(matching.getUpdatedAt(), time) == 7L)
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            List<UserMatching> userMatchings = matching.getUserMatchings();
            for (UserMatching userMatching: userMatchings) {
                if (userMatching.getStatus().equals(MatchingStatus.PROFILE_OPEN)) {
                    userMatching.setStatus(MatchingStatus.PROFILE_READY);
                    sendPushMessage(matching, FcmMessage.PROFILE_OPEN, userMatching.getUser());
                }
            }
        }
    }

    /**
     * 6. 7일 종류 하루 전
     */
    private void checkEndOfMatching(List<Matching> matchings, LocalDateTime time) {
        matchings = matchings.stream()
                .filter(matching -> matching.getStatus().equals(MatchingStatus.MATCHING_CONTINUE))
                .filter(matching -> ChronoUnit.DAYS.between(time, matching.getExpiryTime()) == 1L)
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            sendPushMessage(matching, FcmMessage.LAST_CHAT, null);
        }
    }

    /**
     * 매칭 관련 유저에게 푸쉬 보내기
     */
    private void sendPushMessage(Matching matching, FcmMessage fcmMessage, User exceptUser) {
        List<User> users = matching.getUserMatchings().stream()
                .map(UserMatching::getUser)
                .collect(Collectors.toList());
        for (User user: users) {
            if (!Objects.isNull(exceptUser)) {
                if (user.getId().equals(exceptUser.getId())) {
                    continue;
                }
            }
            firebaseCloudMessageService.sendMessageTo(
                    user.getDeviceId(),
                    fcmMessage.getTitle(),
                    fcmMessage.getBody(),
                    fcmMessage.getPath(),
                    fcmMessage.getType(),
                    matching.getId()
            );
        }
    }
}
