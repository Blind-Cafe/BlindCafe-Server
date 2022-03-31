package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.UserMatching;
import com.example.BlindCafe.domain.type.status.MatchingStatus;
import com.example.BlindCafe.domain.type.status.UserStatus;
import com.example.BlindCafe.repository.MatchingRepository;
import com.example.BlindCafe.repository.UserMatchingRepository;
import com.example.BlindCafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.service.NotificationService.*;

/**
 * 배치 작업을 위한 서비스
 *
 * Job 1 - 10분 단위 작업
 * - Step 1 : 3일 채팅 확인 : 24,48시간 기능 해제 메시지, 3일 채팅 종료 1시간 마감 임박 메시지, 72시간 경과 시 프로필 교환 메시지, 채팅방 생성 후 5분간 토픽이 없는 경우 자동 전송
 * - Step 2 : 7일 채팅 확인 : 만료 시간이 지난 경우 채팅방 비활성화
 * 
 * Job 2  - 1시간 단위 작업
 * - Step 3 : 매칭 요청 유효 시간 초과한 경우 매칭 취소
 * - Step 4 : 메모리로 관리하는 사용자 디바이스 정보 초기화
 *
 * Job 3 - 1일 단위 작업
 * - Step 5 : 7일 채팅에서 종료 1일 전 마감 임박 메시지
 *
 * Job 4 - 매일 자정(밤 12시) 작업
 * - Step 6 : 매칭권 갯수 리셋
 * 
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final MatchingService matchingService;

    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final UserRepository userRepository;

    private static final int DELAY_TEN_MIN = 1000 * 60 * 10;
    private static final int DELAY_ONE_HOUR = 1000 * 60 * 60;
    private static final int DELAY_ONE_DAY = 1000 * 60 * 60 * 24;
    private static final int UNIT = 100;

    /**
     * Name : Job 1 - 10분 단위 작업
     * Allocation : Step 1, Step 2
     */
    @Scheduled(fixedDelay = DELAY_TEN_MIN)
    @Transactional
    public void processFixedTenMin() {
        log.info("[BEGIN] Do Step 1,2 per 10 min");
        LocalDateTime time = LocalDateTime.now();
        List<Matching> matchings = matchingRepository.findByStatusAndActive(MatchingStatus.MATCHING, true);
        checkBasicMatching(time, matchings); // step 1
        checkContinuousMatching(time, matchings); // step 2
        log.info("[END] Do Step 1,2 per 10 min");
    }

    /**
     * Name : Job 2 - 1시간 단위 작업
     * Allocation : Step 3
     */
    @Scheduled(fixedDelay = DELAY_ONE_HOUR)
    @Transactional
    public void processFixedOneHour() {
        log.info("[BEGIN] Do Step 3,4 per 1 hour");
        LocalDateTime time = LocalDateTime.now();
        cancelAgingRequest(time); // step 3
        initializationUserDeviceAtMemory(); // step 4
        log.info("[END] Do Step 3,4 per 1 hour");
    }

    /**
     * Name : Job 3 - 1일 단위 작업
     * Allocation : Step 5
     */
    @Scheduled(fixedDelay = DELAY_ONE_DAY)
    @Transactional
    public void processFixedOneDay() {
        log.info("[BEGIN] Do Step 5 per 1 Day");
        LocalDateTime time = LocalDateTime.now();
        List<Matching> matchings = matchingRepository.findByStatusAndActive(MatchingStatus.MATCHING, true);
        checkEndOfContinuousMatching(time, matchings); // step 5
        log.info("[END] Do Step 5 per 1 Day");
    }

    /**
     * Name : Job 4 - 매일 밤 12시 작업
     * Allocation : Step 6
     * TODO 사용자가 많아질수록 작업 시간이 길어지기 때문에 멀티쓰레드로 작업 필요
     * 현재는 모든 사용자에서 Unit size 단위로 Transaction 단위만 줄여서 작업 실행
     */
    @Scheduled(cron="0 0 00 * * ?")
    public void processEveryMidnight() {
        log.info("[BEGIN] Do Step 6 at Everyday Midnight");
        List<User> users = userRepository.findByStatus(UserStatus.NORMAL);
        int size = users.size();
        int iteration = size / UNIT;
        if (size % UNIT != 0) iteration++;
        for (int i=0; i<iteration; i++) {
            if (i == iteration-1) resetTicket(users.subList(i * UNIT, size));
            else resetTicket(users.subList(i * UNIT, (i+1) * UNIT));
        }
        log.info("[END] Do Step 6 at Everyday Midnight");
    }

    /**
     * Step
     */
    // Step 1 : 3일 채팅 확인 : 24,48시간 기능 해제 메시지, 3일 채팅 종료 1시간 마감 임박 메시지, 72시간 경과 시 프로필 교환 메시지, 채팅방 생성 후 5분간 토픽이 없는 경우 자동 전송
    private void checkBasicMatching(LocalDateTime time, List<Matching> matchings) {
        matchings = matchings.stream()
                .filter(m -> !m.getIsContinuous())
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            matchingService.checkMatchingTime(matching, time);
        }
    }

    // Step 2 : 7일 채팅 확인 : 만료 시간이 지난 경우 채팅방 비활성화
    private void checkContinuousMatching(LocalDateTime time, List<Matching> matchings) {
        matchings = matchings.stream()
                .filter(Matching::getIsContinuous)
                .filter(matching -> matching.getExpiredTime().isBefore(time))
                .collect(Collectors.toList());
        for (Matching matching: matchings) {
            matchingService.checkMatchingTime(matching, time);
        }
    }

    // Step 3 : 매칭 요청이 72시간이 넘었는지 확인 : 초과한 경우 -> 자동 매칭 취소
    private void cancelAgingRequest(LocalDateTime time) {
        List<UserMatching> userMatchings = userMatchingRepository.findAgingMatchingRequests(time.minusDays(3L));
        for (UserMatching userMatching: userMatchings) {
            userMatching.cancel();
        }
    }

    // Step 4 : 메모리로 관리하는 사용자 알림 설정 및 디바이스 정보 초기화
    private void initializationUserDeviceAtMemory() {
        entireNotificationSettingInMemory.clear();
        roomNotificationOffInMemory.clear();
        deviceInfoInMemory.clear();
    }

    // Step 5 : 7일 채팅에서 종료까지 1일 남은 경우 종료 임박 템플릿 전송
    private void checkEndOfContinuousMatching(LocalDateTime time, List<Matching> matchings) {
        matchings = matchings.stream()
                .filter(Matching::getIsContinuous)
                .collect(Collectors.toList());
        for (Matching matching: matchings) {
            matchingService.checkMatchingTime(matching, time);
        }
    }

    // Step 6 : 매일 밤 12시 매칭권 갯수 리셋
    @Transactional
    protected void resetTicket(List<User> users) {
        for (User user: users)
            user.getTicket().init();
    }
}
