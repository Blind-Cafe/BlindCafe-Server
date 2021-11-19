package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.FcmMessage;
import com.example.BlindCafe.type.Gender;
import com.example.BlindCafe.type.status.MatchingStatus;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Enumerated;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.type.Gender.N;
import static com.example.BlindCafe.type.ReasonType.FOR_LEAVE_ROOM;
import static com.example.BlindCafe.type.status.CommonStatus.NORMAL;
import static com.example.BlindCafe.type.status.MatchingStatus.*;
import static java.util.Comparator.comparing;
import static javax.persistence.EnumType.STRING;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchingService {

    private final FirebaseCloudMessageService fcm;

    private final UserRepository userRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final MatchingRepository matchingRepository;
    private final DrinkRepository drinkRepository;
    private final ReasonRepository reasonRepository;

    private final static Long MAX_WAIT_TIME = 24L;
    private final static int BASIC_CHAT_DAYS = 3;
    private final static int EXTEND_CHAT_DAYS = 7;

    /**
     * 내 테이블 조회 - 프로필 교환을 완료한 상대방 목록 조회
     */
    public MatchingListDto getMatchings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        LocalDateTime now = LocalDateTime.now();
        List<MatchingListDto.MatchingDto> matchings = user.getUserMatchings().stream()
                .filter(userMatching -> userMatching.getMatching().getIsContinuous())
                .map(userMatching -> userMatching.getMatching())
                .map(matching -> makeMatchingDto(matching, user, now))
                .filter(matchingDto -> matchingDto != null)
                .collect(Collectors.toList());

        return new MatchingListDto(matchings);
    }

    private MatchingListDto.MatchingDto makeMatchingDto(Matching matching, User user, LocalDateTime now) {
        Long restDay = ChronoUnit.DAYS.between(now, matching.getExpiryTime()) >= 0L ?
                ChronoUnit.DAYS.between(now, matching.getExpiryTime()) : -1L;

        User partner = matching.getUserMatchings().stream()
                .filter(userMatching -> !userMatching.getUser().equals(user))
                .findAny()
                .map(partnerMatching -> partnerMatching.getUser()).orElse(null);

        if (partner != null) {
            return MatchingListDto.MatchingDto.builder()
                    .matchingId(matching.getId())
                    .partner(new MatchingListDto.Partner(partner))
                    .latestMessage("none")
                    .isReceived(true)
                    .expiryDay(restDay)
                    .build();
        } else {
            return null;
        }
    }

    /**
     * 채팅방 정보 조회
     */
    public MatchingDetailDto getMatching(Long userId, Long matchingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser())
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        return new MatchingDetailDto(matching, partner);
    }

    /**
     * 매칭 요청
     */
    @Transactional
    public CreateMatchingDto.Response createMatching(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        if (user.getStatus().equals(UserStatus.NOT_REQUIRED_INFO))
            throw new BlindCafeException(NOT_REQUIRED_INFO_FOR_MATCHING);

        UserMatching partnerMatching = searchAbleMatching(user);

        UserMatching userMatching = UserMatching.builder()
                .user(user)
                .status(WAIT)
                .build();

        if (partnerMatching != null) {
            User partner = partnerMatching.getUser();

            // 매칭 상대를 찾은 경우
            userMatching.setStatus(FOUND);
            partnerMatching.setStatus(FOUND);

            Interest commonInterest = getCommonInterest(
                    partnerMatching,
                    getUserInterestSortedByPriority(user)
            );

            /**
             * Todo
             * 토픽 생성, 확실하게 결정 나면 수정
             */
            List<MatchingTopic> topics = new ArrayList<>();

            Matching matching = Matching.builder()
                    .interest(commonInterest)
                    .topics(topics)
                    .isContinuous(false)
                    .status(MATCHING_NOT_START)
                    .build();
            matching = matchingRepository.save(matching);

            userMatching.setMatching(matching);
            partnerMatching.setMatching(matching);
            userMatchingRepository.save(userMatching);
            userMatchingRepository.save(partnerMatching);

            // FCM
            fcm.sendMessageTo(
                    user.getDeviceId(),
                    FcmMessage.MATCHING.getTitle(),
                    FcmMessage.MATCHING.getBody(),
                    FcmMessage.MATCHING.getPath()
            );
            fcm.sendMessageTo(
                    partner.getDeviceId(),
                    FcmMessage.MATCHING.getTitle(),
                    FcmMessage.MATCHING.getBody(),
                    FcmMessage.MATCHING.getPath()
            );

            return CreateMatchingDto.Response.matchingBuilder()
                    .matchingStatus(userMatching.getStatus())
                    .matchingId(matching.getId())
                    .partnerId(partner.getId())
                    .partnerNickname(partner.getNickname())
                    .build();
        } else {
            userMatchingRepository.save(userMatching);
            return CreateMatchingDto.Response.noneMatchingBuilder()
                    .matchingStatus(userMatching.getStatus())
                    .build();
        }
    }

    /**
     * 매칭이 가능한 상대방이 있는지 확인
     */
    private UserMatching searchAbleMatching(User user) {
        // 유저 관심사 확인
        List<Interest> userInterests = getUserInterestSortedByPriority(user);

        // 유저 관심사 설정이 잘못된 경우
        if (userInterests.size() < 3)
            throw new BlindCafeException(INVALID_INTEREST_SET);

        // 이전 대화 상대 찾기
        List<User> pastPartners = user.getUserMatchings().stream()
                .filter(userMatching ->
                        !userMatching.getStatus().equals(WAIT) &&
                        !userMatching.getStatus().equals(FOUND))
                .map(UserMatching::getMatching)
                .map(matching ->
                        matching.getUserMatchings().stream()
                                .filter(userMatching -> !userMatching.getUser().equals(user))
                                .map(UserMatching::getUser)
                                .findFirst()
                                .orElse(null))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        return userMatchingRepository.findByStatus(WAIT)
                .stream().sorted(comparing(UserMatching::getCreatedAt))
                .filter(otherMatching -> isValidRequestTime(otherMatching, now))
                .filter(otherMatching -> isValidGender(otherMatching, user))
                .filter(otherMatching -> isContainInterest(otherMatching, userInterests))
                .filter(otherMatching -> !isMatched(otherMatching, pastPartners))
                .findFirst().orElse(null);
    }

    /**
     * 유저의 관심사를 우선순위 순으로 정렬
     */
    private List<Interest> getUserInterestSortedByPriority(User user) {
        return user.getInterestOrders().stream()
                .sorted(comparing(InterestOrder::getPriority))
                .map(InterestOrder::getInterest)
                .collect(Collectors.toList());
    }

    /**
     * 유효한 시간 내의 요청인지 확인
     */
    private boolean isValidRequestTime(UserMatching otherMatching, LocalDateTime now) {
        Long diffTime = ChronoUnit.HOURS.between(otherMatching.getCreatedAt(), now);
        if (diffTime <= MAX_WAIT_TIME)
            return true;
        else
            return false;
    }

    /**
     * 유저간 선호하는 성별인지 확인
     */
    private boolean isValidGender(UserMatching userMatching, User user) {
        User otherUser = userMatching.getUser();

        Gender userMyGender = user.getMyGender();
        Gender userPartnerGender = user.getPartnerGender();
        Gender otherUserMyGender = otherUser.getMyGender();
        Gender otherUserPartnerGender = otherUser.getPartnerGender();

        if ((userPartnerGender.equals(otherUserMyGender) ||
                userPartnerGender.equals(N)) &&
            (otherUserPartnerGender.equals(userMyGender) ||
                otherUserPartnerGender.equals(N)))
            return true;
        else
            return false;
    }

    /**
     * 관심사가 공통되는지 확인
     */
    private boolean isContainInterest(UserMatching userMatching, List<Interest> userInterests) {
        List<Interest> otherInterests = userMatching.getUser().getInterestOrders()
                .stream()
                .map(InterestOrder::getInterest)
                .collect(Collectors.toList());

        for (int i=0; i<userInterests.size(); i++) {
            if (otherInterests.contains(userInterests.get(i)))
                return true;
        }
        return false;
    }

    /**
     * 유저간 공통 관심사 추출
     * Todo
     * 나중에 isContainInterest 에서 한 번에 공통 관심사 뽑자
     */
    private Interest getCommonInterest(UserMatching userMatching, List<Interest> userInterests) {
        List<Interest> otherInterests = userMatching.getUser().getInterestOrders()
                .stream()
                .map(InterestOrder::getInterest)
                .collect(Collectors.toList());

        for (int i=0; i<userInterests.size(); i++) {
            if (otherInterests.contains(userInterests.get(i)))
                return userInterests.get(i);
        }
        return null;
    }

    private boolean isMatched(UserMatching userMatching, List<User> pastPartners) {
        return pastPartners.contains(userMatching.getUser());
    }


    /**
     * 매칭에 대한 음료수 선택하기
     */
    @Transactional
    public DrinkDto.Response setDrink(Long userId, Long matchingId, DrinkDto.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Drink drink = drinkRepository.findById(request.getDrink())
                .orElseThrow(() -> new BlindCafeException(NO_DRINK));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        UserMatching userMatching = matching.getUserMatchings()
                .stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findAny()
                .orElseThrow(() -> new BlindCafeException(NO_USER_MATCHING));

        userMatching.setDrink(drink);
        userMatching.setStatus(MATCHING);

        if (!matching.getStatus().equals(MATCHING)) {
            LocalDateTime now = LocalDateTime.now();
            matching.setStatus(MATCHING);
            matching.setStartTime(now);
            matching.setExpiryTime(now.plusDays(BASIC_CHAT_DAYS));
        }

        userMatchingRepository.save(userMatching);
        matchingRepository.save(matching);

        LocalDateTime ldt = matching.getStartTime();
        Timestamp timestamp = Timestamp.valueOf(ldt);
        String startTime = String.valueOf(timestamp.getTime() / 1000);

        return DrinkDto.Response.builder()
                .codeAndMessage(SUCCESS)
                .startTime(startTime)
                .build();
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

    /**
     * 매칭 취소하기
     */
    @Transactional
    public void cancelMatching(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        UserMatching userMatching = user.getUserMatchings().stream()
                .filter(um -> um.getStatus().equals(WAIT))
                .findFirst()
                .orElseThrow(() -> new BlindCafeException(NO_REQUEST_MATCHING));

        userMatching.setStatus(CANCEL_REQUEST);
    }

    /**
     * 프로필 교환 시 내 프로필 조회
     */
    public MatchingProfileDto getMatchingProfile(Long userId, Long matchingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        if (!matching.getStatus().equals(MATCHING_EXPIRED))
            throw new BlindCafeException(NOT_YET_EXCHANGE_PROFILE);

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser() )
                .findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        ProfileImage profileImage = user.getProfileImages()
                .stream().sorted(Comparator.comparing(ProfileImage::getPriority))
                .filter(pi -> pi.getStatus().equals(NORMAL))
                .findFirst()
                .orElse(null);
        String src = profileImage != null ?
                profileImage.getSrc() : null;
        String region = user.getAddress() != null ?
                user.getAddress().toString() : null;

        boolean isFill = (src == null || region == null) ? false : true;

        return MatchingProfileDto.builder()
                .partnerNickname(partner.getNickname())
                .isFill(isFill)
                .profileImage(src)
                .nickname(user.getNickname())
                .region(region)
                .gender(user.getMyGender())
                .age(user.getAge())
                .build();
    }
}
