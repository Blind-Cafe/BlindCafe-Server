package me.blindcafe.blindcafe.service;

import me.blindcafe.blindcafe.domain.Matching;
import me.blindcafe.blindcafe.domain.MatchingTopic;
import me.blindcafe.blindcafe.domain.topic.Audio;
import me.blindcafe.blindcafe.domain.topic.Image;
import me.blindcafe.blindcafe.domain.topic.Subject;
import me.blindcafe.blindcafe.domain.topic.Topic;
import me.blindcafe.blindcafe.domain.type.MessageType;
import me.blindcafe.blindcafe.dto.chat.MessageDto;
import me.blindcafe.blindcafe.dto.response.TopicResponse;
import me.blindcafe.blindcafe.exception.BlindCafeException;
import me.blindcafe.blindcafe.repository.TopicRepository;
import me.blindcafe.blindcafe.utils.MatchingMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static me.blindcafe.blindcafe.exception.CodeAndMessage.EMPTY_TOPIC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;

    private final MatchingMessageUtil matchingMessageUtil;

    private static final int TOPIC_COMMON_QUANTITY = 10;
    private static final int TOPIC_OTHER_QUANTITY = 2;
    private static final int TOPIC_DIFF_QUANTITY = 6;
    private static final int TOPIC_IMAGE_QUANTITY = 5;
    private static final int TOPIC_AUDIO_QUANTITY = 4;

    private static final Long SUBJECT_LIMIT = 1000L;
    private static final Long AUDIO_LIMIT = 2000L;

    public static final Long PUBLIC_INTEREST_ID = 0L;

    // 관심사 기반으로 토픽 생성 - 공통 관심사
    // 일상 질문 10개, 공통관심사 10개, 그 외 관심사 각 2개, 이미지 5개, 오디오 4개
    @Transactional
    public MatchingTopic makeTopicBySimilarInterest(Long interestId) {
        List<Topic> topics = new ArrayList<>();
        // 일상 질문
        topics.addAll(getTopicByInterest(PUBLIC_INTEREST_ID, TOPIC_COMMON_QUANTITY));
        // 공통 관심사
        topics.addAll(getTopicByInterest(interestId, TOPIC_COMMON_QUANTITY));
        // 그 외 관심사
        List<Long> ids = new ArrayList<>();
        ids.add(interestId);
        topics.addAll(getTopicByInterestNotIn(ids));
        // 이미지
        topics.addAll(getImageTopicList());
        // 오디오
        topics.addAll(getAudioTopicList());
        return MatchingTopic.create(topics);
    }

    // 관심사 기반으로 토픽 생성 - 관심사가 다른 경우
    // 일상 질문 10개, 각자 관심사 6개, 그 외 관심사 각 2개, 이미지 5개, 오디오 4개
    @Transactional
    public MatchingTopic makeTopicByDifferentInterest(Long interestId1, Long interestId2) {
        List<Topic> topics = new ArrayList<>();
        // 일상 질문
        topics.addAll(getTopicByInterest(PUBLIC_INTEREST_ID, TOPIC_COMMON_QUANTITY));
        // 각자 관심사
        topics.addAll(getTopicByInterest(interestId1, TOPIC_DIFF_QUANTITY));
        topics.addAll(getTopicByInterest(interestId2, TOPIC_DIFF_QUANTITY));
        // 그 외 관심사
        List<Long> ids = new ArrayList<>();
        ids.add(interestId1);
        ids.add(interestId2);
        topics.addAll(getTopicByInterestNotIn(ids));
        // 이미지
        topics.addAll(getImageTopicList());
        // 오디오
        topics.addAll(getAudioTopicList());
        return MatchingTopic.create(topics);
    }

    // 토픽 가져온 후 섞어서 뽑기
    private List<Topic> getTopicByInterest(Long interestId, int quantity) {
        return getTopicsWithShuffle(topicRepository.findSubjectByInterestId(interestId), quantity);
    }

    // 나머지 관심사에서 섞어서 뽑기
    private List<Topic> getTopicByInterestNotIn(List<Long> ids) {
        List<Topic> topics = new ArrayList<>();
        Map<Long, List<Subject>> topicMap = new HashMap<>();

        List<Subject> findTopics = topicRepository.findSubjectByInterestIdNotIN(ids);
        // 관심사 id 기준으로 map에 저장
        findTopics.forEach(ft -> {
            List<Subject> tempTopicList = topicMap.getOrDefault(ft.getInterestId(), new ArrayList<>());
            tempTopicList.add(ft);
            topicMap.put(ft.getInterestId(), tempTopicList);
        });

        // 관심사 id 기준으로 섞어서 입력받은 수만큼 추출
        topicMap.keySet().forEach(key ->
                topics.addAll(getTopicsWithShuffle(topicMap.get(key), TOPIC_OTHER_QUANTITY)));

        return topics;
    }

    // 이미지 토픽 가져오기
    private List<Topic> getImageTopicList() {
        return getTopicsWithShuffle(topicRepository.findImages(), TOPIC_IMAGE_QUANTITY);
    }

    // 오디오 토픽 가져오기
    private List<Topic> getAudioTopicList() {
        return getTopicsWithShuffle(topicRepository.findAudios(), TOPIC_AUDIO_QUANTITY);
    }

    // 셔플 후 원하는 수량만큼 뽑기
    private List<Topic> getTopicsWithShuffle(Collection<? extends Topic> topics, int quantity) {
        List<Topic> topicList = new ArrayList<>(topics);
        Collections.shuffle(topicList);
        return topicList.subList(0, quantity);
    }

    /**
     * 가장 최근에 조회한 토픽 가져오기
     */
    public TopicResponse getTopic(Matching matching) {
        Long topicId = matching.getTopic().getLatestTopic();
        if (topicId == null) return null;

        LocalDateTime access = matching.getTopic().getAccess();

        int topicType = getTopicType(topicId);
        switch (topicType) {
            case 0:
                Subject subject = getSubject(topicId);
                return TopicResponse.fromSubject(subject, MessageType.TEXT_TOPIC, access);
            case 1:
                Audio audio = getAudio(topicId);
                return TopicResponse.fromAudio(audio, MessageType.AUDIO_TOPIC, access);
            default:
                Image image = getImage(topicId);
                return TopicResponse.fromImage(image, MessageType.IMAGE_TOPIC, access);
        }
    }

    /**
     * 다음 토픽 가져와서 메시지폼으로 만들기
     */
    public MessageDto getNextTopic(Long mid, Long topicId) {
        int topicType = getTopicType(topicId);
        switch (topicType) {
            case 0:
                Subject subject = getSubject(topicId);
                return matchingMessageUtil.sendTopic(mid, MessageType.TEXT_TOPIC, subject.getSubject());
            case 1:
                Audio audio = getAudio(topicId);
                return matchingMessageUtil.sendTopic(mid, MessageType.AUDIO_TOPIC, audio.getSrc());
            default:
                Image image = getImage(topicId);
                return matchingMessageUtil.sendTopic(mid, MessageType.IMAGE_TOPIC, image.getSrc());
        }
    }

    // 토픽 ID로 토픽 종류 확인
    public int getTopicType(Long topicId) {
        if (topicId <= SUBJECT_LIMIT) return 0;
        if (topicId <= AUDIO_LIMIT) return 1;
        return 2;
    }

    public Subject getSubject(Long topicId) {
        return topicRepository.findSubjectById(topicId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_TOPIC));
    }

    public Audio getAudio(Long topicId) {
        return topicRepository.findAudioById(topicId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_TOPIC));
    }

    public Image getImage(Long topicId) {
        return topicRepository.findImageById(topicId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_TOPIC));
    }
}
