package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.*;
import com.example.BlindCafe.dto.request.ExchangeProfileRequest;
import com.example.BlindCafe.dto.request.OpenProfileRequest;
import com.example.BlindCafe.dto.request.SelectDrinkRequest;
import com.example.BlindCafe.dto.response.MatchingDetailResponse;
import com.example.BlindCafe.dto.response.MatchingListResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.domain.type.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.domain.type.Gender.N;
import static com.example.BlindCafe.domain.type.ReasonType.FOR_LEAVE_ROOM;
import static com.example.BlindCafe.service.TopicService.PUBLIC_INTEREST_ID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchingService {

    private final TopicService topicService;

    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final MatchingHistoryRepository matchingHistoryRepository;
    private final TicketRepository ticketRepository;
    private final DrinkRepository drinkRepository;
    private final ReasonRepository reasonRepository;
    private final CustomReasonRepository customReasonRepository;

    private final MessageRepository messageRepository;
    private final RoomLogRepository roomLogRepository;

    /**
     * 매칭 요청
     */
    @Transactional
    public void createMatching(Long userId) {
        
        // 매칭 요청 중인 경우 핸들링
        if (isMatchingRequest(userId))
            throw new BlindCafeException(ALREADY_MATCHING_REQUEST);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));

        // 사용자 관심사 조회
        List<Long> interests = user.getMainInterests().stream()
                .map(Interest::getId)
                .collect(Collectors.toList());

        // 매칭 요청 만들기
        UserMatching myMatching = UserMatching.create(user, interests);

        // 사용자 매칭 히스토리 조회
        List<Long> partners = matchingHistoryRepository.findByUserId(userId).getMatchingPartners();

        // 매칭 풀에서 매칭 전적이 없는 사용자들의 매칭 요청 조회
        List<UserMatching> ableMatchingRequests =
                userMatchingRepository.findAbleMatchingRequests(partners).stream()
                    .filter(um -> isValidGender(um, user)) // 성별 필터링
                    .collect(Collectors.toList());

        // 매칭 요청 풀에 매칭 가능한 요청이 없는 경우 매칭 요청 풀에 저장
        if (ableMatchingRequests.size() == 0) {
            userMatchingRepository.save(myMatching);
            return;
        }

        // 관심사가 일치하는 요청있는지 확인
        UserMatching matchingCandidate = ableMatchingRequests.stream()
                .filter(um -> isContainInterest(interests, um.getInterests()) != 0L)
                .findFirst().orElse(null);

        // 관심사가 일치하는 요청이 없는 경우 매칭 요청 풀에서 랜덤으로 배정
        if (Objects.isNull(matchingCandidate)) {
            Random random = new Random();
            matchingCandidate = ableMatchingRequests.get(random.nextInt(ableMatchingRequests.size()));
        }

        List<UserMatching> userMatchings = new ArrayList<>();
        userMatchings.add(myMatching);
        userMatchings.add(matchingCandidate);

        String partnerInterest = matchingCandidate.getInterests();
        Interest similarInterest = user.getMainInterests().stream()
                .filter(i -> i.getId().equals(isContainInterest(interests, partnerInterest)))
                .findAny().orElse(null);

        // 토픽 생성
        MatchingTopic topic;
        if (similarInterest != null) {
            // 공통 관심사가 있는 경우
            topic = topicService.makeTopicBySimilarInterest(similarInterest.getId());
        } else {
            // 관심사가 다른 경우
            Long partnerFirstInterestId = Long.parseLong(partnerInterest.split(",")[0]);
            topic = topicService.makeTopicByDifferentInterest(interests.get(0), partnerFirstInterestId);
        }

        // 매칭 생성
        Matching matching = Matching.create(userMatchings, similarInterest, topic);
        matchingRepository.save(matching);

        // TODO 접속 유무에 따라 메시지 퍼블리싱 또는 FCM 전송
    }

    // 관심사가 일치하는 요청있는지 확인
    private Long isContainInterest(List<Long> myInterest, String partnerInterest) {
        String[] split = partnerInterest.split(",");
        for (Long interestId : myInterest)
            for (String s : split)
                if (interestId.toString().equals(s)) return interestId;
        return PUBLIC_INTEREST_ID;
    }

    // 선호하는 성별인지 확인
    private boolean isValidGender(UserMatching userMatching, User user) {
        User otherUser = userMatching.getUser();

        Gender userMyGender = user.getMyGender();
        Gender userPartnerGender = user.getPartnerGender();
        Gender otherUserMyGender = otherUser.getMyGender();
        Gender otherUserPartnerGender = otherUser.getPartnerGender();

        return (userPartnerGender.equals(otherUserMyGender) ||
                userPartnerGender.equals(N)) &&
                (otherUserPartnerGender.equals(userMyGender) ||
                        otherUserPartnerGender.equals(N));
    }

    /**
     * 매칭 취소하기
     */
    @Transactional
    public void cancelMatching(Long userId) {
        UserMatching userMatching = userMatchingRepository.findMatchingRequestByUserId(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING_REQUEST));
        userMatching.cancel();
    }

    /**
     * 채팅방 리스트 조회
     */
    public MatchingListResponse getMatchings(Long userId) {
        
        // 요청중인 매칭 조회
        boolean request = isMatchingRequest(userId);

        // 존재하는 매칭 조회
        List<Matching> matchings = userMatchingRepository.findMatchingByUserId(userId).stream()
                .map(UserMatching::getMatching)
                .collect(Collectors.toList());

        Map<Long, MatchingListResponse.MatchingInfo> matchingInfoMap = new HashMap<>();
        for (Matching m: matchings) {
            MatchingListResponse.MatchingInfo info = MatchingListResponse.MatchingInfo.fromEntity(m, userId);
            matchingInfoMap.put(m.getId(), info);
        }

        // 채팅방 정렬을 위해서 채팅방 메시지 기록 조회
        List<Long> matchingIds = matchings.stream()
                .map(Matching::getId)
                .collect(Collectors.toList());

        List<MatchingListResponse.RoomHistory> sortedMatchingList = new ArrayList<>();
        for (Long mid: matchingIds) {
            // 최근 메시지 내용, 시간 조회
            Message message = messageRepository.findFirstByMatchingIdOrderByCreatedAtDesc(mid);
            // 채팅방 접속 기록 조회
            RoomLog log = roomLogRepository.findFirstByMatchingIdAndUserIdOrderByAccessAtDesc(mid, userId)
                    .orElse(null);
            sortedMatchingList.add(MatchingListResponse.RoomHistory.fromEntities(message, log));
        }

        // 시간 순으로 내림차순 정렬
        sortedMatchingList = sortedMatchingList.stream()
                .sorted(Comparator.comparing(MatchingListResponse.RoomHistory::getCreatedAt).reversed())
                .collect(Collectors.toList());

        List<MatchingListResponse.MatchingInfo> blind = new ArrayList<>();
        List<MatchingListResponse.MatchingInfo> bright = new ArrayList<>();
        for (MatchingListResponse.RoomHistory m: sortedMatchingList) {
            // 최근 메세지 및 수신 여부 추가
            Long matchingId = m.getMatchingId();
            MatchingListResponse.MatchingInfo info = matchingInfoMap.get(matchingId);
            info.updateHistory(m);
            // 3일, 7일 채팅에 따라서 분류
            if (info.isBlind()) blind.add(info);
            else bright.add(info);
        }

        return new MatchingListResponse(request, blind, bright);
    }

    /**
     * 채팅방 정보 조회
     */
    @Transactional
    public MatchingDetailResponse getMatching(Long userId, Long matchingId) {

        Matching matching = matchingRepository.findValidMatchingById(matchingId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        return MatchingDetailResponse.fromEntity(matching, userId);
    }

    /**
     * 음료수 선택하기
     */
    @Transactional
    public void selectDrink(Long userId, SelectDrinkRequest request) {

        Matching matching = matchingRepository.findValidMatchingById(request.getMatchingId())
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        UserMatching userMatching = matching.getUserMatchings().stream()
                .filter(um -> um.getUser().getId().equals(userId))
                .findAny().orElseThrow(() -> new BlindCafeException(NON_AUTHORIZATION_MATCHING));

        // 음료를 이미 선택한 경우
        if (userMatching.getDrink() != null)
            throw new BlindCafeException(ALREADY_SELECT_DRINK);

        Drink drink = drinkRepository.findById(request.getDrink())
                .orElseThrow(() -> new BlindCafeException(EMPTY_DRINK));

        // 음료 선택
        userMatching.selectDrink(drink);

        // TODO 음료 선택 메시지 publish
    }

    /**
     * 토픽 가져오기
     */
    @Transactional
    public void getTopic(Long matchingId) {

        Matching matching = matchingRepository.findValidMatchingById(matchingId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        // 토픽 아이디 가져오기
        Long topicId = matching.getTopic();

        // 토픽 전송하기
        topicService.sendTopic(matchingId, topicId);
    }

    /**
     * 프로필 공개 수락/거절하기
     */
    @Transactional
    public void openProfile(Long userId, OpenProfileRequest request) {
        
        // 사용자 프로필 작성 여부 확인
        if (!isValidProfile(userId))
            throw new BlindCafeException(NOT_REQUIRED_INFO_FOR_MATCHING);

        Matching matching = matchingRepository.findValidMatchingById(request.getMatchingId())
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        boolean isAccept = request.isAccept();
        // 프로필 공개
        boolean result = matching.getUserMatchingById(userId).openProfile(isAccept);

        if (!isAccept) {
            // TODO 프로필 공개 거절 메시지 Publish
            
            return;
        }

        // TODO 프로필 공개 수락 메시지 Publish


        if (result) {
            // TODO 프로필 교환 메시지 Publish
        }
    }

    // 사용자 프로필 작성 여부 확인
    private boolean isValidProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return user.getMainAvatar() != null
                && user.getAddress() != null
                && user.getVoice() != null
                && user.getMbti() != null;
    }

    /**
     * 프로필 교환 수락/거절하기
     */
    @Transactional
    public void exchangeProfile(Long userId, ExchangeProfileRequest request) {

        Matching matching = matchingRepository.findValidMatchingById(request.getMatchingId())
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        boolean isAccept = request.isAccept();
        // 프로필 교환
        boolean result = matching.getUserMatchingById(userId).exchangeProfile(isAccept);

        if (!isAccept) {
            // TODO 프로필 교환 거절 메시지 Publish
        }

        // TODO 프로필 교환 메시지 Publish

        // 모두 프로필 공개를 수락한 경우
        if (result) {
            // TODO 7일 채팅 메시지 Publish
        }
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveMatching(Long userId, Long matchingId, Long reasonId) {

        Matching matching = matchingRepository.findValidMatchingById(matchingId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        Reason reason = reasonRepository.findByReasonTypeAndNum(FOR_LEAVE_ROOM, reasonId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_REASON));

        User user = matching.getUserMatchingById(userId).getUser();

        // 채팅방 나가기
        matching.leave(userId);

        // 방 나간 사유 저장
        CustomReason customReason = CustomReason.create(user, matchingId, reason);
        customReasonRepository.save(customReason);

        // TODO 방 나간 사유 메시지 publish
    }

    /**
     * 채팅방 로그 남기기
     */
    @Transactional
    public void createRoomLog(Long userId, Long matchingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        RoomLog roomLog = RoomLog.builder()
                .user(user)
                .matching(matching)
                .latestTime(LocalDateTime.now())
                .build();
        roomLogRepository.save(roomLog);
    }

    /**
     * 매칭권 관련
     */
    // 매칭권 생성
    @Transactional
    public void createTickets(User user) {
        Ticket newTicket = Ticket.create(user);
        ticketRepository.save(newTicket);
    }

    // 현재 가지고 있는 매칭권 수 조회
    public int getTicketCount(Long userId) {
        Ticket ticket = ticketRepository.findByUserId(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return ticket.getCount();
    }

    // 현재 요청 중인 매칭이 있는지 조회
    public boolean isMatchingRequest(Long userId) {
        Optional<UserMatching> matchingRequest =
                userMatchingRepository.findMatchingRequestByUserId(userId);
        return !Objects.isNull(matchingRequest);
    }

    // 매칭 히스토리 테이블 만들기
    public void createMatchingHistory(User user) {
        MatchingHistory matchingHistory = MatchingHistory.create(user);
        matchingHistoryRepository.save(matchingHistory);
    }
}
