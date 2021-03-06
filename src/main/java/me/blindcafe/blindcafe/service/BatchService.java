package me.blindcafe.blindcafe.service;

import me.blindcafe.blindcafe.domain.*;
import me.blindcafe.blindcafe.domain.type.Gender;
import me.blindcafe.blindcafe.domain.type.status.MatchingStatus;
import me.blindcafe.blindcafe.domain.type.status.UserStatus;
import me.blindcafe.blindcafe.repository.*;
import me.blindcafe.blindcafe.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static me.blindcafe.blindcafe.service.NotificationService.*;

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
 * Job 5 - 매일 밤 (새벽 2시) 작업
 * - Step 7 : 전날 하루동안 접속한 사용자 수 및 접속 비율 저장
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final MatchingService matchingService;

    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final UserRepository userRepository;
    private final ConnectLogRepository connectLogRepository;
    private final DailyConnectRepository dailyConnectRepository;

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
     * Step 6
     * - 사용자가 많아질수록 작업 시간이 길어지기 때문에 멀티쓰레드로 작업 필요
     * - 현재는 모든 사용자에서 Unit size 단위로 Transaction 단위만 줄여서 작업 실행
     */
    @Scheduled(cron="0 0 00 * * ?")
    public void processEveryMidnight1() {
        log.info("[BEGIN] Do Step 6 at Everyday Midnight");
        List<User> users = userRepository.findByStatus(UserStatus.NORMAL);
        users = users.stream().filter(u -> !u.isAdmin()).collect(Collectors.toList());
        int size = users.size();
        int iteration = size / UNIT;
        if (size % UNIT != 0) iteration++;
        for (int i=0; i<iteration; i++) {
            if (i == iteration-1) resetTicket(users.subList(i * UNIT, size));
            else resetTicket(users.subList(i * UNIT, (i+1) * UNIT));
        }
        log.info("[END] Do Step 6 at Everyday Midnight");
    }

    @Scheduled(cron="0 0 02 * * ?")
    public void processEveryMidnight2() {
        log.info("[BEGIN] Do Step 7 at Everyday Midnight");
        saveDailyConnect();
        log.info("[END] Do Step 7 at Everyday Midnight");
    }

    /**
     * Step
     */
    // Step 1 : 3일 채팅 확인 : 24,48시간 기능 해제 메시지, 3일 채팅 종료 1시간 마감 임박 메시지, 72시간 경과 시 프로필 교환 메시지, 채팅방 생성 후 5분간 토픽이 없는 경우 자동 전송
    private void checkBasicMatching(LocalDateTime time, List<Matching> matchings) {
        matchings = matchings.stream()
                .filter(m -> !m.isContinuous())
                .collect(Collectors.toList());
        for (Matching matching : matchings) {
            matchingService.checkMatchingTime(matching, time);
        }
    }

    // Step 2 : 7일 채팅 확인 : 만료 시간이 지난 경우 채팅방 비활성화
    private void checkContinuousMatching(LocalDateTime time, List<Matching> matchings) {
        matchings = matchings.stream()
                .filter(Matching::isContinuous)
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
                .filter(Matching::isContinuous)
                .collect(Collectors.toList());
        for (Matching matching: matchings) {
            matchingService.checkMatchingTime(matching, time);
        }
    }

    // Step 6 : 매일 밤 12시 매칭권 갯수 리셋
    @Transactional
    public void resetTicket(List<User> users) {
        for (User user: users)
            user.getTicket().init();
    }
    
    // Step 7 : 매일 밤 12시 하루동안 접속한 사용자 수 및 접속 비율 저장
    @Transactional
    public void saveDailyConnect() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1L);

        // 어제 전체 접속 사용자 수 조회
        List<ConnectLog> connects =
                connectLogRepository.findByAccessDay(yesterday.format(DateTimeUtil.dateFormatter));

        List<Long> ids = connects.stream()
                .map(ConnectLog::getUserId)
                .collect(Collectors.toList());
        
        List<User> users = userRepository.findAllById(ids);
        Long entire = (long) users.size();
        // 성별 구분
        Long male = users.stream()
                .filter(u -> u.getMyGender().equals(Gender.M))
                .count();

        DailyConnect dailyConnect = DailyConnect.create(yesterday, entire, male);
        dailyConnectRepository.save(dailyConnect);
    }
}
