package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.*;
import com.example.BlindCafe.dto.chat.MessageDto;
import com.example.BlindCafe.dto.request.ExchangeProfileRequest;
import com.example.BlindCafe.dto.request.SelectDrinkRequest;
import com.example.BlindCafe.dto.response.MatchingDetailResponse;
import com.example.BlindCafe.dto.response.MatchingListResponse;
import com.example.BlindCafe.dto.response.TopicResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.utils.MatchingMessageUtil;
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
    private final ChatService chatService;

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

    private final MatchingMessageUtil matchingMessageUtil;

    /**
     * 매칭 요청
     */
    @Transactional
    public void createMatching(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));

        // 매칭 요청 중인 경우 핸들링
        if (isMatchingRequest(user))
            throw new BlindCafeException(ALREADY_MATCHING_REQUEST);

        // 사용자 관심사 조회
        List<Long> interests = user.getMainInterests().stream()
                .map(Interest::getId)
                .collect(Collectors.toList());

        // 매칭 요청 만들기
        UserMatching myMatching = UserMatching.create(user, interests);

        // 사용자 매칭 히스토리 조회
        List<Long> partners = matchingHistoryRepository.findByUserId(userId).getMatchingPartners();

        // 매칭 풀에서 매칭 전적이 없는 사용자들의 매칭 요청 조회
        String partnerIds = stringFromList(partners);
        List<UserMatching> ableMatchingRequests =
                userMatchingRepository.findAbleMatchingRequests(partnerIds).stream()
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
        String interestName;
        if (similarInterest != null) {
            // 공통 관심사가 있는 경우
            topic = topicService.makeTopicBySimilarInterest(similarInterest.getId());
            // 공통 관심사명
            interestName = similarInterest.getName();
        } else {
            // 관심사가 다른 경우
            Long partnerFirstInterestId = Long.parseLong(partnerInterest.split(",")[0]);
            topic = topicService.makeTopicByDifferentInterest(interests.get(0), partnerFirstInterestId);
            interestName = null;
        }

        // 매칭 생성
        MatchingPush push = MatchingPush.create();
        Matching matching = Matching.create(userMatchings, similarInterest, topic, push);
        matching = matchingRepository.save(matching);

        // 매칭 성공 이벤트 Publish
        MessageDto message = matchingMessageUtil.successMatching(matching.getId(), interestName);
        chatService.publish(String.valueOf(matching.getId()), message);
    }
    
    // 리스트 -> 문자열 변환
    private String stringFromList(List<Long> ids) {
        if (ids.size() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (Long id: ids)
            sb.append(id).append(",");
        String result = sb.toString();
        return result.substring(0, result.length()-1);
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        
        // 요청중인 매칭 조회
        boolean request = isMatchingRequest(user);

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
            Message message = messageRepository.findFirstByMatchingIdOrderByCreatedAtDesc(mid.toString());

            // 채팅방 접속 기록 조회
            RoomLog log = roomLogRepository.findRoomLogByMatchingId(mid.toString());
            String access = null;
            if (log != null && log.getAccess().containsKey(userId.toString()))
                access = log.getAccess().get(userId.toString());
            sortedMatchingList.add(MatchingListResponse.RoomHistory.fromEntities(message, access));
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
            info.setHistory(m);
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

        // 채팅방 정보 조회 시 3일 채팅에서 72시간 넘은 경우 프로필 교환 메시지 전송
        // (배치 작업으로 채팅방 상태를 업데이트해주지만 배치 텀으로 인해 최신화 안된 경우를 해결하기 위해)
        checkMatchingTime(matching, LocalDateTime.now());
        
        // 가장 최근에 조회한 토픽 확인
        TopicResponse topic = topicService.getTopic(matching);

        return MatchingDetailResponse.fromEntity(matching, userId, topic);
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

        Drink drink = drinkRepository.findById(request.getDrink())
                .orElseThrow(() -> new BlindCafeException(EMPTY_DRINK));

        // 음료 선택
        userMatching.selectDrink(drink);

        // 음료 선택 이벤트 Publish
        String username = userMatching.getUser().getNickname();
        String drinkName = drink.getName();
        MessageDto message = matchingMessageUtil.selectDrink(matching.getId(), username, drinkName);
        chatService.publish(String.valueOf(matching.getId()), message);
    }

    /**
     * 토픽 가져오기
     */
    @Transactional
    public void getTopic(Long matchingId) {

        Matching matching = matchingRepository.findValidMatchingById(matchingId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        // 토픽 가져오기
        Long topicId = matching.getNextTopic();

        // 토픽 전송 퍼블리싱
        MessageDto message = topicService.getNextTopic(matchingId, topicId);
        chatService.publish(String.valueOf(matching.getId()), message);
    }

    /**
     * 프로필 교환 수락/거절하기
     */
    @Transactional
    public void exchangeProfile(Long userId, ExchangeProfileRequest request) {

        Matching matching = matchingRepository.findValidMatchingById(request.getMatchingId())
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        User user = matching.getUserMatchingById(userId).getUser();

        // 사용자 프로필 작성 여부 확인
        isValidProfile(user);

        boolean isBothAccept = matching.getUserMatchingById(userId).exchangeProfile();

        // 프로필 교환 메시지 Publish
        MessageDto message = matchingMessageUtil.exchangeProfile(matching.getId(), user.getId(), user.getNickname());
        chatService.publish(String.valueOf(matching.getId()), message);

        // 양 쪽 모두 프로필을 공개한 경우 교환
        if (isBothAccept) {
            // 프로필 교환 성공(7일 채팅) 메시지 Publish
            message = matchingMessageUtil.successExchange(matching.getId());
            chatService.publish(String.valueOf(matching.getId()), message);

            // 음료 획득 메시지 Publish
            for (UserMatching um: matching.getUserMatchings()) {
                User u = um.getUser();
                Drink d = um.getDrink();
                message = matchingMessageUtil.takeDrink(matching.getId(), u.getNickname(), d.getName());
                chatService.publish(String.valueOf(matching.getId()), message);
            }
        }
    }

    // 사용자 프로필 작성 여부 확인
    private void isValidProfile(User user) {
        // "이 때 프로필에서 `사진`, `나이`, `성별`, `지역`이 필수설정되어야 전송가능하다.
        if (user.getMainAvatar() == null)
            throw new BlindCafeException(REQUIRED_AVATAR);
        if (user.getAddress() == null)
            throw new BlindCafeException(REQUIRED_ADDRESS);
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
        User partner = matching.getUserMatchings().stream()
                .map(UserMatching::getUser)
                .filter(u -> !u.equals(user))
                .findAny().orElse(null);

        // 채팅방 나가기
        matching.leave(userId);
        
        // Partner가 null인 경우 먼저 방을 나갔거나 탈퇴 회원
        if (partner != null) {
            // 방 나간 사유 저장
            CustomReason customReason = CustomReason.create(user, matchingId, reason);
            customReasonRepository.save(customReason);

            // 방 나가기 메시지 Publish

            MessageDto message = matchingMessageUtil.leaveMatching(matchingId, user.getNickname(), partner.getNickname(),reason.getText());
            chatService.publish(String.valueOf(matchingId), message);
        }
    }

    /**
     * 매칭권 관련
     */
    // 현재 가지고 있는 매칭권 수 조회
    public int getTicketCount(User user) {
        Ticket ticket = ticketRepository.findByUser(user)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return ticket.getCount();
    }

    // 현재 요청 중인 매칭이 있는지 조회
    public boolean isMatchingRequest(User user) {
        Optional<UserMatching> matchingRequest =
                userMatchingRepository.findMatchingRequestByUserId(user.getId());
        return matchingRequest.isPresent();
    }

    /**
     * 매칭 내에서 진행 관련
     */
    // 3일 채팅에서 매칭 지속 시간에 따른 이벤트 퍼블리싱
    @Transactional
    public void checkMatchingTime(Matching matching, LocalDateTime time) {
        if (!matching.isActive()) return;
        
        // 3일 채팅
        if (!matching.getIsContinuous()) {
            checkMatchingFunction(matching, time); // 24,48시간 기능 해제 메시지
            checkEndOfBasicMatching(matching, time); // 3일 채팅 종료 1시간 메시지
            checkExchangeProfile(matching, time); // 프로필 교환 메시지
            checkSendFirstTopic(matching, time); // 채팅방 생성 후 5분간 토픽이 없는 경우 자동 전송
            return;
        }
        
        // 7일 채팅
        checkEndOfContinuousMatching(matching, time);
        expiry(matching, time);
    }

    // 3일 채팅에서 24/48시간 지났는지 확인 -> 사진/목소리 기능 해제 메시지 전송
    private void checkMatchingFunction(Matching matching, LocalDateTime time) {
        int result = matching.sendMatchingFunction(time);
        if (result > 0) {
            MessageDto message = matchingMessageUtil.sendMatchingFunction(matching.getId(), result);
            chatService.publish(String.valueOf(matching.getId()), message);
        }
    }

    // 3일 채팅에서 종료 1시간 전인지 확인 -> 마감 임박 메시지 전송
    private void checkEndOfBasicMatching(Matching matching, LocalDateTime time) {
        if (matching.sendEndOfBasicMatching(time)) {
            MessageDto message = matchingMessageUtil.sendEndOfBasicMatching(matching.getId());
            chatService.publish(String.valueOf(matching.getId()), message);
        }
    }

    // 3일 채팅에서 72시간 지났는지 확인 -> 프로필 교환 템플릿 전송
    private void checkExchangeProfile(Matching matching, LocalDateTime time) {
        if (matching.sendExchangeProfile(time)) {
            MessageDto message = matchingMessageUtil.sendExchangeProfile(matching.getId());
            chatService.publish(String.valueOf(matching.getId()), message);
        }
    }

    // 채팅방 생성 후 5분간 토픽이 없는 경우 자동 전송
    private void checkSendFirstTopic(Matching matching, LocalDateTime time) {
        if (matching.sendFirstTopic(time)) {
            // 토픽 전송
            getTopic(matching.getId());
        }
    }

    // 7일 채팅에서 종료까지 1일 남은 경우 종료 임박 템플릿 전송
    private void checkEndOfContinuousMatching(Matching matching, LocalDateTime time) {
        if (matching.checkEndOfContinuousMatching(time)) {
            MessageDto message = matchingMessageUtil.sendEndOfContinuousMatching(matching.getId());
            chatService.publish(String.valueOf(matching.getId()), message);
        }
    }

    // 7일 채팅 만료 시간이 지난 경우 채팅방 비활성화
    private void expiry(Matching matching, LocalDateTime time) {
        matching.expiry(time);
    }
}
