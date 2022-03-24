package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.domain.*;
import com.example.BlindCafe.dto.request.ExchangeProfileRequest;
import com.example.BlindCafe.dto.request.SelectDrinkRequest;
import com.example.BlindCafe.dto.response.MatchingDetailResponse;
import com.example.BlindCafe.dto.response.MatchingListResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import com.example.BlindCafe.firebase.FirebaseService;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.domain.type.FcmMessage;
import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.domain.type.status.MatchingStatus;
import com.example.BlindCafe.domain.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.domain.type.Gender.N;
import static com.example.BlindCafe.domain.type.ReasonType.FOR_LEAVE_ROOM;
import static com.example.BlindCafe.domain.type.ReasonType.FOR_WONT_EXCHANGE_PROFILE;
import static com.example.BlindCafe.domain.type.status.CommonStatus.NORMAL;
import static com.example.BlindCafe.domain.type.status.MatchingStatus.*;
import static com.example.BlindCafe.service.TopicService.PUBLIC_INTEREST_ID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchingService {

    private final TopicService topicService;

    private final MatchingRepository matchingRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final MatchingHistoryRepository matchingHistoryRepository;
    private final TicketRepository ticketRepository;
    private final DrinkRepository drinkRepository;

    private final MessageRepository messageRepository;
    private final RoomLogRepository roomLogRepository;



    

    private final FirebaseService firebaseService;
    private final FirebaseCloudMessageService fcmService;

    private final UserRepository userRepository;



    private final ReasonRepository reasonRepository;

    private final static int EXTEND_CHAT_DAYS = 7;

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
     * 프로필 교환 수락/거절하기
     */
    @Transactional
    public void exchangeProfile(Long userId, ExchangeProfileRequest request) {

        Matching matching = matchingRepository.findValidMatchingById(request.getMatchingId())
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        // 프로필 교환
        boolean exchangeResult = matching.getUserMatchingById(userId).exchangeProfile(request.isAccept());

        // 프로필 공개를 수락한 경우
        if (request.isAccept()) {
            // TODO 프로필 공개 메시지 Publish

            // 모두 프로필 공개를 수락한 경우
            if (exchangeResult) {
                // TODO 7일 채팅 메시지 Publish
            }
            return;
        }

        // 프로필 공개를 거절한 경우
        // TODO 프로필 공개 거절 메시지 Publish

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
        if (Objects.isNull(matchingRequest)) return false;
        else return true;
    }

    // 매칭 히스토리 테이블 만들기
    public void createMatchingHistory(User user) {
        MatchingHistory matchingHistory = MatchingHistory.create(user);
        matchingHistoryRepository.save(matchingHistory);
    }







    private String getFirstDescription(User user, User partner, Interest interest) {
        return user.getNickname() + "님과 " + partner.getNickname() + "님이 선택한 <" + interest.getName() + "> 테이블입니다.";
    }


    private String getDrinkDescription(User user, Drink drink) {
        return drink.getName() + "를 주문한 " + user.getNickname() + "님입니다.\n반갑게 맞아주세요.";
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public DeleteMatchingDto deleteMatching(Long userId, Long matchingId, Long reasonNum) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        Reason reason = reasonRepository.findByReasonTypeAndNum(FOR_LEAVE_ROOM, reasonNum)
                .orElseThrow(() -> new BlindCafeException(NO_REASON));

        // 상대방에게 사유 저장, 상태 변경
        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user)).findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));
        partnerMatching.setReason(reason);
        partnerMatching.setStatus(FAILED_LEAVE_ROOM);

        // 현재 matching 비활성화
        matching.setStatus(FAILED_LEAVE_ROOM);

        // 내 user matching 끊어주기
        UserMatching userMatching = matching.getUserMatchings().stream()
                .filter(um -> um.getUser().equals(user))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));
        userMatching.setStatus(OUT);

        return DeleteMatchingDto.builder()
                .codeAndMessage(SUCCESS)
                .build();
    }



    @Transactional
    public OpenMatchingProfileDto.Response openMatchingProfile(Long userId, Long matchingId, OpenMatchingProfileDto.Request request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(UserStatus.NORMAL))
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        // 닉네임 수정
        user.setNickname(request.getNickname());
        // 지역 수정
        user.setAddress(new Address(request.getState(), request.getRegion()));

        UserMatching userMatching = matching.getUserMatchings().stream()
                .filter(um -> um.getUser().equals(user)).findAny()
                .orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));
        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user)).findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        userMatching.setStatus(PROFILE_READY);

        boolean result = false;
        if (partnerMatching.getStatus().equals(PROFILE_READY)) {
            result = true;
            // fcm
            User partner = matching.getUserMatchings().stream()
                    .filter(um -> !um.getUser().equals(user))
                    .map(um -> um.getUser() )
                    .findAny()
                    .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

            fcmService.sendMessageTo(
                    partner.getDeviceId(),
                    FcmMessage.PROFILE_OPEN.getTitle(),
                    FcmMessage.PROFILE_OPEN.getBody(),
                    FcmMessage.PROFILE_OPEN.getPath(),
                    FcmMessage.PROFILE_OPEN.getType(),
                    0L,
                    null
            );
            matching.getPush().setPush_profile_open(true);
        }

        return OpenMatchingProfileDto.Response.builder()
                .result(result)
                .nickname(partnerMatching.getUser().getNickname())
                .build();
    }

    /**
     * 상대방 프로필 조회하기
     */
    public MatchingProfileDto getPartnerProfile(Long userId, Long matchingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        if (!matching.getStatus().equals(PROFILE_EXCHANGE))
            throw new BlindCafeException(NOT_YET_EXCHANGE_PROFILE);

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser() )
                .findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(um -> um.getUser().equals(partner))
                .findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        if (partnerMatching.getStatus().equals(PROFILE_OPEN)) {
            return MatchingProfileDto.builder()
                    .fill(false)
                    .nickname(partner.getNickname())
                    .build();
        }

        return makeProfile(partner, user);
    }

    private MatchingProfileDto makeProfile(User user, User partner) {
        Avatar avatar = user.getAvatars()
                .stream().sorted(Comparator.comparing(Avatar::getPriority))
                .filter(pi -> pi.getStatus().equals(NORMAL))
                .findFirst()
                .orElse(null);

        String src = Objects.isNull(avatar) ? null : avatar.getSrc();
        String region = Objects.isNull(user.getAddress()) ? null : user.getAddress().toString();

        List<String> interests = user.getInterestOrders().stream()
                .sorted(Comparator.comparing(InterestOrder::getPriority))
                .map(io -> io.getInterest())
                .map(Interest::getName)
                .collect(Collectors.toList());

        boolean fill = (src == null || region == null) ? false : true;

        return MatchingProfileDto.builder()
                .fill(fill)
                .userId(user.getId())
                .partnerNickname(partner.getNickname())
                .profileImage(src)
                .nickname(user.getNickname())
                .region(region)
                .gender(user.getMyGender())
                .interests(interests)
                .age(user.getAge())
                .build();
    }

    @Transactional
    public OpenMatchingProfileDto.Response acceptPartnerProfile(Long userId, Long matchingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        UserMatching userMatching = matching.getUserMatchings().stream()
                .filter(um -> um.getUser().equals(user))
                .findAny().orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));

        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(um -> !um.equals(userMatching))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        MatchingStatus myMatchingStatus = userMatching.getStatus();

        if (!myMatchingStatus.equals(PROFILE_READY)) {
            // 거절 당한 경우
            throw new BlindCafeException(REJECT_PROFILE_EXCHANGE);
        }

        // 1. profile_accept 으로 user matching 변경
        userMatching.setStatus(PROFILE_ACCEPT);

        // 2. 상대방 user matching 확인
        User partner = partnerMatching.getUser();
        if (partner.getStatus().equals(UserStatus.SUSPENDED) || partner.getStatus().equals(UserStatus.RETIRED)) {
            partnerMatching.setStatus(PROFILE_ACCEPT);
        }
        MatchingStatus partnerMatchingStatus = partnerMatching.getStatus();

        // 2-1. 상대방 아직 수락 안 했으면 대기
        if (partnerMatchingStatus.equals(PROFILE_READY)) {
            return OpenMatchingProfileDto.Response.builder()
                    .result(false)
                    .nickname(partner.getNickname())
                    .build();
        }

        // 2-2. 상대방 수락했으면 7일방으로 + 7일 만료 다시 세팅
        userMatching.setStatus(MATCHING_CONTINUE);
        partnerMatching.setStatus(MATCHING_CONTINUE_YET);

        LocalDateTime now = LocalDateTime.now();
        matching.setIsContinuous(true);
        matching.setStatus(MATCHING_CONTINUE);
        matching.setStartTime(now);
        matching.setExpiryTime(now.plusDays(EXTEND_CHAT_DAYS));

        // 2-2-1. 양쪽 다 음료(뱃지) 추가 (내 음료로 수정)
        UserDrink myDrink = UserDrink.builder()
                .user(user)
                .drink(userMatching.getDrink())
                .build();
        UserDrink partnerDrink = UserDrink.builder()
                .user(partner)
                .drink(partnerMatching.getDrink())
                .build();

        user.getUserDrinks().add(myDrink);
        partner.getUserDrinks().add(partnerDrink);

        // 메세지 db에 저장
        insertDrink(matching, user, myDrink);
        insertDrink(matching, partner, partnerDrink);

        fcmService.sendMessageTo(
                partner.getDeviceId(),
                FcmMessage.MATCHING_CONTINUE.getTitle(),
                FcmMessage.MATCHING_CONTINUE.getBody(),
                FcmMessage.MATCHING_CONTINUE.getPath(),
                FcmMessage.MATCHING_CONTINUE.getType(),
                0L,
                null
        );
        matching.getPush().setPush_matching_continue(true);

        return OpenMatchingProfileDto.Response.builder()
                .result(true)
                .nickname(partner.getNickname())
                .build();
    }

    private void insertDrink(Matching matching, User user, UserDrink userDrink) {
        Message message = new Message();
        message.setMatching(matching);
        message.setUser(user);
        message.setContents(userDrink.getDrink().getName());
        message.setType(MessageType.DRINK);
        Message savedMessage = messageRepository.save(message);

        // 메세지 firestore 저장
        LocalDateTime ldt = savedMessage.getCreatedAt();
        Timestamp timestamp = Timestamp.valueOf(ldt);

        FirestoreDto firestoreDto = FirestoreDto.builder()
                .roomId(matching.getId())
                .targetToken(null)
                .message(new FirestoreDto.FirestoreMessage(
                        Long.toString(savedMessage.getId()),
                        Long.toString(user.getId()),
                        user.getNickname(),
                        savedMessage.getContents(),
                        MessageType.DRINK.getFirestoreType(),
                        timestamp
                ))
                .build();
        firebaseService.insertMessage(firestoreDto);
    }

    @Transactional
    public void rejectExchangeProfile(Long userId, Long matchingId, Long reasonNum) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        Reason reason = reasonRepository.findByReasonTypeAndNum(FOR_WONT_EXCHANGE_PROFILE, reasonNum)
                .orElseThrow(() -> new BlindCafeException(NO_REASON));

        UserMatching userMatching = user.getUserMatchings().stream()
                .filter(um -> um.getMatching().equals(matching))
                .findAny().orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));

        // 1. 내 요청 OUT 으로 변경
        userMatching.setStatus(OUT);
        // 2. matching 상태 변경
        matching.setStatus(FAILED_WONT_EXCHANGE);

        // 3. 상대방 user matching 터진 status + 사유 추가
        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(um -> !um.equals(userMatching))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));
        partnerMatching.setStatus(FAILED_WONT_EXCHANGE);
        partnerMatching.setReason(reason);
    }

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
}
