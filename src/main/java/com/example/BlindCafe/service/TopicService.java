package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.MatchingTopic;
import com.example.BlindCafe.domain.topic.Audio;
import com.example.BlindCafe.domain.topic.Image;
import com.example.BlindCafe.domain.topic.Subject;
import com.example.BlindCafe.domain.topic.Topic;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.BlindCafe.exception.CodeAndMessage.EMPTY_TOPIC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;

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
        Map<Long, List<Topic>> topicMap = new HashMap<>();

        List<Topic> findTopics = topicRepository.findSubjectByInterestIdNotIN(ids);
        // 관심사 id 기준으로 map에 저장
        findTopics.forEach(ft -> {
            List<Topic> tempTopicList = topicMap.getOrDefault(ft.getId(), new ArrayList<>());
            tempTopicList.add(ft);
            topicMap.put(ft.getId(), tempTopicList);
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
    private List<Topic> getTopicsWithShuffle(List<Topic> topics, int quantity) {
        Collections.shuffle(topics);
        return topics.subList(0, quantity);
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
