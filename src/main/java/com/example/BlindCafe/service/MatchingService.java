package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.entity.topic.Audio;
import com.example.BlindCafe.entity.topic.Image;
import com.example.BlindCafe.entity.topic.Subject;
import com.example.BlindCafe.entity.topic.Topic;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import com.example.BlindCafe.firebase.FirebaseService;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.FcmMessage;
import com.example.BlindCafe.type.Gender;
import com.example.BlindCafe.type.MessageType;
import com.example.BlindCafe.type.status.MatchingStatus;
import com.example.BlindCafe.type.status.TopicStatus;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.type.Gender.N;
import static com.example.BlindCafe.type.ReasonType.FOR_LEAVE_ROOM;
import static com.example.BlindCafe.type.ReasonType.FOR_WONT_EXCHANGE_PROFILE;
import static com.example.BlindCafe.type.status.CommonStatus.NORMAL;
import static com.example.BlindCafe.type.status.MatchingStatus.*;
import static java.util.Comparator.comparing;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchingService {

    private final FirebaseService firebaseService;
    private final FirebaseCloudMessageService fcmService;

    private final UserRepository userRepository;
    private final UserMatchingRepository userMatchingRepository;
    private final MatchingRepository matchingRepository;
    private final DrinkRepository drinkRepository;
    private final ReasonRepository reasonRepository;
    private final TopicRepository topicRepository;
    private final RoomLogRepository roomLogRepository;
    private final MessageRepository messageRepository;

    private final static Long MAX_WAIT_TIME = 24L;
    private final static int BASIC_CHAT_DAYS = 3;
    private final static int EXTEND_CHAT_DAYS = 7;
    private final static Long PUBLIC_INTEREST_ID = 0L;
    private final static Long MAX_INTEREST_ID = 9L;
    private final static Long SUBJECT_LIMIT = 1000L;
    private final static Long AUDIO_LIMIT = 2000L;
    private final static Long IMAGE_LIMIT = 3000L;

    /**
     * 내 테이블 조회 - 프로필 교환을 완료한 상대방 목록 조회
     */
    public MatchingListDto getMatchings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        LocalDateTime now = LocalDateTime.now();
        List<MatchingListDto.MatchingDto> matchings = user.getUserMatchings().stream()
                .filter(userMatching -> !Objects.isNull(userMatching.getMatching()))
                .filter(userMatching -> userMatching.getMatching().getIsContinuous())
                .map(userMatching -> userMatching.getMatching())
                .map(matching -> makeMatchingDto(matching, user, now))
                .filter(matchingDto -> !Objects.isNull(matchingDto))
                .collect(Collectors.toList());

        return new MatchingListDto(matchings);
    }

    private MatchingListDto.MatchingDto makeMatchingDto(Matching matching, User user, LocalDateTime now) {
        Long restDay = ChronoUnit.DAYS.between(now, matching.getExpiryTime()) >= 0L ?
                ChronoUnit.DAYS.between(now, matching.getExpiryTime()) : -1L;

        String expiryTime = "";
        if (restDay > 0L) {
            expiryTime = restDay + "일 남음";
        } else if (restDay == 0L) {
            expiryTime = ChronoUnit.HOURS.between(now, matching.getExpiryTime()) + "시간 남음";
        } else {
            expiryTime = "만료";
        }

        User partner = matching.getUserMatchings().stream()
                .filter(userMatching -> !userMatching.getUser().equals(user))
                .findAny()
                .map(partnerMatching -> partnerMatching.getUser()).orElse(null);

        if (partner != null) {
            return MatchingListDto.MatchingDto.builder()
                    .matchingId(matching.getId())
                    .partner(new MatchingListDto.Partner(partner))
                    .latestMessage("none")
                    .received(true)
                    .expiryTime(expiryTime)
                    .build();
        } else {
            return null;
        }
    }

    /**
     * 채팅방 정보 조회
     */
    @Transactional
    public MatchingDetailDto getMatching(Long userId, Long matchingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));
        updateMatchingStatus(matching);

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser())
                .filter(u -> u.getStatus().equals(UserStatus.NORMAL))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        ProfileImage profileImage = partner.getProfileImages()
                .stream().sorted(Comparator.comparing(ProfileImage::getPriority))
                .filter(pi -> pi.getStatus().equals(NORMAL))
                .findFirst()
                .orElse(null);
        String src = profileImage != null ? profileImage.getSrc() : null;

        Drink drink = matching.getUserMatchings().stream()
                .filter(um -> um.getUser().equals(partner))
                .filter(um -> !Objects.isNull(um.getDrink()))
                .map(UserMatching::getDrink)
                .findAny()
                .orElse(null);

        LocalDateTime ldt = matching.getStartTime();
        Timestamp timestamp = Timestamp.valueOf(ldt);
        String startTime = String.valueOf(timestamp.getTime() / 1000);

        return MatchingDetailDto.builder()
                .matchingId(matching.getId())
                .profileImage(src)
                .nickname(partner.getNickname())
                .drink(drink == null ? "미입력" : drink.getName())
                .startTime(startTime)
                .interest(matching.getInterest().getName())
                .isContinuous(matching.getIsContinuous())
                .build();
    }

    private void updateMatchingStatus(Matching matching) {
        if (!matching.getStatus().equals(MATCHING))
            return;
        if (matching.getExpiryTime().isAfter(LocalDateTime.now()))
            return;
        List<UserMatching> userMatchings = matching.getUserMatchings();
        for (UserMatching userMatching: userMatchings) {
            userMatching.setStatus(PROFILE_OPEN);
        }
        matching.setStatus(PROFILE_EXCHANGE);
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

            Matching matching = Matching.builder()
                    .interest(commonInterest)
                    .isContinuous(false)
                    .startTime(LocalDateTime.now())
                    .status(MATCHING_NOT_START)
                    .build();

            // 토픽 생성, 확실하게 결정 나면 수정
            // subject 36개, image 5개, audio 4개
            List<MatchingTopic> matchingTopics = makeMatchingTopics(matching);

            matching.setTopics(matchingTopics);

            matching = matchingRepository.save(matching);

            userMatching.setMatching(matching);
            partnerMatching.setMatching(matching);
            userMatchingRepository.save(userMatching);
            userMatchingRepository.save(partnerMatching);

            // FCM
            fcmService.sendMessageTo(
                    user.getDeviceId(),
                    FcmMessage.MATCHING.getTitle(),
                    FcmMessage.MATCHING.getBody(),
                    FcmMessage.MATCHING.getPath(),
                    FcmMessage.MATCHING.getType(),
                    0L
            );
            fcmService.sendMessageTo(
                    partner.getDeviceId(),
                    FcmMessage.MATCHING.getTitle(),
                    FcmMessage.MATCHING.getBody(),
                    FcmMessage.MATCHING.getPath(),
                    FcmMessage.MATCHING.getType(),
                    0L
            );

            // 메세지 db에 저장
            User admin = userRepository.findById(0L).orElseThrow(() -> new BlindCafeException(NO_USER));
            Message message = new Message();
            message.setMatching(matching);
            message.setUser(admin);
            message.setContents(getFirstDescription(user, partner, matching.getInterest()));
            message.setType(MessageType.DESCRIPTION);
            Message savedMessage = messageRepository.save(message);

            // 메세지 firestore 저장
            LocalDateTime ldt = savedMessage.getCreatedAt();
            Timestamp timestamp = Timestamp.valueOf(ldt);

            FirestoreDto firestoreDto = FirestoreDto.builder()
                    .roomId(matching.getId())
                    .targetToken(partner.getDeviceId())
                    .message(new FirestoreDto.FirestoreMessage(
                            Long.toString(savedMessage.getId()),
                            Long.toString(admin.getId()),
                            admin.getNickname(),
                            savedMessage.getContents(),
                            MessageType.DESCRIPTION.getFirestoreType(),
                            timestamp
                    ))
                    .build();
            firebaseService.insertMessage(firestoreDto);

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

    private List<MatchingTopic> makeMatchingTopics(Matching matching) {
        Long interestId = matching.getInterest().getId();
        List<Topic> topics = new ArrayList<>();
        List<MatchingTopic> matchingTopics = new ArrayList<>();

        // 공통 질문
        topics.addAll(getTopicsWithShuffle(
                topicRepository.findByInterestId(PUBLIC_INTEREST_ID), 10));
        // 공통 관심사 질문
        topics.addAll(getTopicsWithShuffle(
                topicRepository.findByInterestId(interestId), 10));
        // 그 외 관심사 질문
        for (Long i = 1L; i<=MAX_INTEREST_ID; i++) {
            if (i != interestId)
                topics.addAll(getTopicsWithShuffle(
                        topicRepository.findByInterestId(i), 2));
        }
        // 이미지 5개
        topics.addAll(getTopicsWithShuffle(
                topicRepository.findImages(), 5));
        // 오디오 4개
        topics.addAll(getTopicsWithShuffle(
                topicRepository.findAudios(), 4));
        Collections.shuffle(topics);

        for (int index=0; index<topics.size(); index++) {
            matchingTopics.add(MatchingTopic.builder()
                    .matching(matching)
                    .topic(topics.get(index))
                    .sequence(index)
                    .status(TopicStatus.WAIT)
                    .build());
        }
        return matchingTopics;
    }

    private List<Topic> getTopicsWithShuffle(List<Topic> topics, int count) {
        Collections.shuffle(topics);
        return topics.subList(0, count);
    }

    private String getFirstDescription(User user, User partner, Interest interest) {
        return user.getNickname() + "님과 " + partner.getNickname() + "님이 선택한 <" + interest.getName() + "> 테이블입니다.";
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
                .filter(userMatching -> !Objects.isNull(userMatching.getMatching()))
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

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser() )
                .findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        UserMatching userMatching = matching.getUserMatchings()
                .stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findAny()
                .orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));

        userMatching.setDrink(drink);
        userMatching.setStatus(MATCHING);

        // 메세지 db에 저장
        User admin = userRepository.findById(0L).orElseThrow(() -> new BlindCafeException(NO_USER));
        Message message = new Message();
        message.setMatching(matching);
        message.setUser(admin);
        message.setContents(getDrinkDescription(user, drink));
        message.setType(MessageType.DESCRIPTION);
        Message savedMessage = messageRepository.save(message);

        // 메세지 firestore 저장
        LocalDateTime ldt = savedMessage.getCreatedAt();
        Timestamp timestamp = Timestamp.valueOf(ldt);

        FirestoreDto firestoreDto = FirestoreDto.builder()
                .roomId(matching.getId())
                .targetToken(partner.getDeviceId())
                .message(new FirestoreDto.FirestoreMessage(
                        Long.toString(savedMessage.getId()),
                        Long.toString(admin.getId()),
                        admin.getNickname(),
                        savedMessage.getContents(),
                        MessageType.DESCRIPTION.getFirestoreType(),
                        timestamp
                ))
                .build();
        firebaseService.insertMessage(firestoreDto);


        if (!matching.getStatus().equals(MATCHING)) {
            LocalDateTime now = LocalDateTime.now();
            matching.setStatus(MATCHING);
            matching.setStartTime(now);
            matching.setExpiryTime(now.plusDays(BASIC_CHAT_DAYS));

            // fcm
            fcmService.sendMessageTo(
                    partner.getDeviceId(),
                    FcmMessage.MATCHING_OPEN.getTitle(),
                    FcmMessage.MATCHING_OPEN.getBody(),
                    FcmMessage.MATCHING_OPEN.getPath(),
                    FcmMessage.MATCHING_OPEN.getType(),
                    0L
            );
        }

        userMatchingRepository.save(userMatching);
        matchingRepository.save(matching);

        String startTime = String.valueOf(timestamp.getTime() / 1000);

        return DrinkDto.Response.builder()
                .codeAndMessage(SUCCESS)
                .startTime(startTime)
                .build();
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

        if (!matching.getStatus().equals(PROFILE_EXCHANGE))
            throw new BlindCafeException(NOT_YET_EXCHANGE_PROFILE);

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser() )
                .findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        return makeProfile(user, partner);
    }

    @Transactional
    public TopicDto getTopic(Long userId, Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        matching.getUserMatchings().stream().
                filter(um -> um.getUser().getId().equals(userId))
                .findAny()
                .orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));

        /**
         * Todo
         * 지금 프록시에서 instanceof로 클래스 타입 확인이 안 돼서
         * 일단은 topicRepository 조회해서 클래스 타입 확인
         * 나중에 matchingTopic -> getTopic을 instanceof로 클래스 타입 확인하기
         */
        MatchingTopic matchingTopic = matching.getTopics().stream()
                .filter(mt -> mt.getStatus().equals(TopicStatus.WAIT))
                .sorted(Comparator.comparing(MatchingTopic::getSequence))
                .findFirst()
                .orElseThrow(() -> new BlindCafeException(EXCEED_MATCHING_TOPIC));
        Long topicId = matchingTopic.getTopic().getId();
        matchingTopic.setStatus(TopicStatus.SELECT);

        if (topicId <= SUBJECT_LIMIT) {
            Subject subject = topicRepository.findSubjectById(topicId)
                    .orElseThrow(() -> new BlindCafeException(INVALID_TOPIC));
            insertTopic(matching, subject.getSubject(), MessageType.TEXT_TOPIC);
            return TopicDto.builder()
                    .type("text")
                    .text(TopicDto.SubjectDto.builder()
                            .content(subject.getSubject()).build())
                    .build();
        } else if (topicId <= AUDIO_LIMIT) {
            Audio audio = topicRepository.findAudioById(topicId)
                    .orElseThrow(() -> new BlindCafeException(INVALID_TOPIC));
            insertTopic(matching, audio.getSrc(), MessageType.AUDIO_TOPIC);
            return TopicDto.builder()
                    .type("audio")
                    .audio(TopicDto.ObjectDto.builder()
                            .answer(audio.getTitle())
                            .src(audio.getSrc()).build())
                    .build();
        } else {
            Image image = topicRepository.findImageById(topicId)
                    .orElseThrow(() -> new BlindCafeException(INVALID_TOPIC));
            insertTopic(matching, image.getSrc(), MessageType.IMAGE_TOPIC);
            return TopicDto.builder()
                    .type("image")
                    .image(TopicDto.ObjectDto.builder()
                            .answer(image.getTitle())
                            .src(image.getSrc()).build())
                    .build();
        }
    }

    private void insertTopic(Matching matching, String contents, MessageType messageType) {
        // 메세지 db에 저장
        User admin = userRepository.findById(0L)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        Message message = new Message();
        message.setMatching(matching);
        message.setUser(admin);
        message.setContents(contents);
        message.setType(messageType);
        Message savedMessage = messageRepository.save(message);

        // 메세지 firestore 저장
        LocalDateTime ldt = savedMessage.getCreatedAt();
        Timestamp timestamp = Timestamp.valueOf(ldt);

        FirestoreDto firestoreDto = FirestoreDto.builder()
                .roomId(matching.getId())
                .targetToken(null)
                .message(new FirestoreDto.FirestoreMessage(
                        Long.toString(savedMessage.getId()),
                        Long.toString(admin.getId()),
                        admin.getNickname(),
                        savedMessage.getContents(),
                        messageType.getFirestoreType(),
                        timestamp
                ))
                .build();
        firebaseService.insertMessage(firestoreDto);
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
                    user.getDeviceId(),
                    FcmMessage.PROFILE_OPEN.getTitle(),
                    FcmMessage.PROFILE_OPEN.getBody(),
                    FcmMessage.PROFILE_OPEN.getPath(),
                    FcmMessage.PROFILE_OPEN.getType(),
                    0L
            );
            fcmService.sendMessageTo(
                    partner.getDeviceId(),
                    FcmMessage.PROFILE_OPEN.getTitle(),
                    FcmMessage.PROFILE_OPEN.getBody(),
                    FcmMessage.PROFILE_OPEN.getPath(),
                    FcmMessage.PROFILE_OPEN.getType(),
                    0L
            );
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
        ProfileImage profileImage = user.getProfileImages()
                .stream().sorted(Comparator.comparing(ProfileImage::getPriority))
                .filter(pi -> pi.getStatus().equals(NORMAL))
                .findFirst()
                .orElse(null);
        String src = Objects.isNull(profileImage) ? null : profileImage.getSrc();
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

        UserMatching userMatching = user.getUserMatchings().stream()
                .filter(um -> um.getMatching().equals(matching))
                .findAny().orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));

        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(um -> !um.equals(userMatching))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        User partner = partnerMatching.getUser();

        MatchingStatus myMatchingStatus = userMatching.getStatus();

        if (!myMatchingStatus.equals(PROFILE_READY)) {
            // 거절 당한 경우
            throw new BlindCafeException(REJECT_PROFILE_EXCHANGE);
        }

        // 1. profile_accept 으로 user matching 변경
        userMatching.setStatus(PROFILE_ACCEPT);

        // 2. 상대방 user matching 확인
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
        partnerMatching.setStatus(MATCHING_CONTINUE);

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
                user.getDeviceId(),
                FcmMessage.MATCHING_CONTINUE.getTitle(),
                FcmMessage.MATCHING_CONTINUE.getBody(),
                FcmMessage.MATCHING_CONTINUE.getPath(),
                FcmMessage.MATCHING_CONTINUE.getType(),
                0L
        );
        fcmService.sendMessageTo(
                partner.getDeviceId(),
                FcmMessage.MATCHING_CONTINUE.getTitle(),
                FcmMessage.MATCHING_CONTINUE.getBody(),
                FcmMessage.MATCHING_CONTINUE.getPath(),
                FcmMessage.MATCHING_CONTINUE.getType(),
                0L
        );

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
