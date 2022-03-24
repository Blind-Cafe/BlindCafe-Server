package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.MatchingTopic;
import com.example.BlindCafe.domain.topic.Audio;
import com.example.BlindCafe.domain.topic.Image;
import com.example.BlindCafe.domain.topic.Subject;
import com.example.BlindCafe.domain.topic.Topic;
import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.BlindCafe.exception.CodeAndMessage.EMPTY_TOPIC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private static final int TOPIC_COMMON_QUANTITY = 10;
    private static final int TOPIC_OTHER_QUANTITY = 2;
    private static final int TOPIC_DIFF_QUANTITY = 6;
    private static final int TOPIC_IMAGE_QUANTITY = 5;
    private static final int TOPIC_AUDIO_QUANTITY = 4;

    private final static Long SUBJECT_LIMIT = 1000L;
    private final static Long AUDIO_LIMIT = 2000L;

    public static final Long PUBLIC_INTEREST_ID = 0L;

    private final TopicRepository topicRepository;

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
        topics.addAll(getTopicByInterestNotIn(ids, TOPIC_OTHER_QUANTITY));
        // 이미지
        topics.addAll(getImageTopic(TOPIC_IMAGE_QUANTITY));
        // 오디오
        topics.addAll(getAudioTopic(TOPIC_AUDIO_QUANTITY));
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
        topics.addAll(getTopicByInterestNotIn(ids, TOPIC_OTHER_QUANTITY));
        // 이미지
        topics.addAll(getImageTopic(TOPIC_IMAGE_QUANTITY));
        // 오디오
        topics.addAll(getAudioTopic(TOPIC_AUDIO_QUANTITY));
        return MatchingTopic.create(topics);
    }

    // 토픽 가져온 후 섞어서 뽑기
    private List<Topic> getTopicByInterest(Long interestId, int quantity) {
        return getTopicsWithShuffle(topicRepository.findSubjectByInterestId(interestId), quantity);
    }

    // 나머지 관심사에서 섞어서 뽑기
    private List<Topic> getTopicByInterestNotIn(List<Long> ids, int quantity) {
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
        topicMap.keySet().forEach(key -> {
            topics.addAll(getTopicsWithShuffle(topicMap.get(key), quantity));
        });

        return topics;
    }

    // 이미지 토픽 가져오기
    private List<Topic> getImageTopic(int quantity) {
        return getTopicsWithShuffle(topicRepository.findImages(), quantity);
    }

    // 오디오 토픽 가져오기
    private List<Topic> getAudioTopic(int quantity) {
        return getTopicsWithShuffle(topicRepository.findAudios(), quantity);
    }

    // 셔플 후 원하는 수량만큼 뽑기
    private List<Topic> getTopicsWithShuffle(List<Topic> topics, int quantity) {
        Collections.shuffle(topics);
        return topics.subList(0, quantity);
    }

    // 토픽 전송하기
    public void sendTopic(Long matchingId, Long topicId) {

        Topic topic = topicRepository.findById(topicId).orElseThrow();

        // TODO 매칭방에 토픽 전송하기
        
        if (topicId <= SUBJECT_LIMIT) {
            Subject subject = topicRepository.findSubjectById(topicId)
                    .orElseThrow(() -> new BlindCafeException(EMPTY_TOPIC));

        } else if (topicId <= AUDIO_LIMIT) {
            Audio audio = topicRepository.findAudioById(topicId)
                    .orElseThrow(() -> new BlindCafeException(EMPTY_TOPIC));

        } else {
            Image image = topicRepository.findImageById(topicId)
                    .orElseThrow(() -> new BlindCafeException(EMPTY_TOPIC));

        }
    }
}
