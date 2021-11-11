package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.CreateMatchingDto;
import com.example.BlindCafe.dto.DrinkDto;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.FcmMessage;
import com.example.BlindCafe.type.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.type.Gender.N;
import static com.example.BlindCafe.type.status.MatchingStatus.*;
import static java.util.Comparator.comparing;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchingService {

    private final FirebaseCloudMessageService fcm;

    private final UserRepository userRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final MatchingRepository matchingRepository;
    private final DrinkRepository drinkRepository;

    private final static Long MAX_WAIT_TIME = 24L;

    @Transactional
    public CreateMatchingDto.Response createMatching(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

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
                    .isValid(true)
                    .status(MATCHING_NOT_START)
                    .build();
            matching = matchingRepository.save(matching);

            userMatching.setMatching(matching);
            partnerMatching.setMatching(matching);
            userMatchingRepository.save(userMatching);
            userMatchingRepository.save(partnerMatching);

            /**
             * Todo
             * FCM
             */
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

    private List<Interest> getUserInterestSortedByPriority(User user) {
        return user.getInterestOrders().stream()
                .sorted(comparing(InterestOrder::getPriority))
                .map(InterestOrder::getInterest)
                .collect(Collectors.toList());
    }

    private boolean isValidRequestTime(UserMatching otherMatching, LocalDateTime now) {
        Long diffTime = ChronoUnit.HOURS.between(otherMatching.getCreatedAt(), now);
        if (diffTime <= MAX_WAIT_TIME)
            return true;
        else
            return false;
    }

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
            matching.setStatus(MATCHING);
            matching.setStartTime(LocalDateTime.now());
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
}
