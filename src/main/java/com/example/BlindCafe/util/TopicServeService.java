package com.example.BlindCafe.util;

import com.example.BlindCafe.dto.FirestoreDto;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.entity.topic.Subject;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.firebase.FirebaseService;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.entity.type.MessageType;
import com.example.BlindCafe.entity.type.status.MatchingStatus;
import com.example.BlindCafe.entity.type.status.TopicStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.service.MatchingService.SUBJECT_LIMIT;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicServeService {

    private final MatchingRepository matchingRepository;
    private final MatchingTopicRepository matchingTopicRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final FirebaseService firebaseService;

    private final static int WAIT_TIME = 1000 * 60 * 3;

    @Async
    @Transactional
    public void serveFirstTopic(Long matchingId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(WAIT_TIME);

                    Matching matching = matchingRepository.findById(matchingId).get();

                    if (matching.getStatus().equals(MatchingStatus.MATCHING)) {
                        List<MatchingTopic> matchingTopics =
                                matchingTopicRepository.findAllByMatching(matching).stream()
                                        .sorted(Comparator.comparing(MatchingTopic::getSequence))
                                        .collect(Collectors.toList());
                        if (matchingTopics.stream()
                                .findFirst()
                                .get().getStatus().equals(TopicStatus.WAIT) &&
                            matchingTopics.stream()
                                .filter(mt -> mt.getTopic().getId() <= SUBJECT_LIMIT)
                                .sorted(Comparator.comparing(MatchingTopic::getSequence))
                                .findFirst().get().getStatus().equals(TopicStatus.WAIT)
                        ) {
                            MatchingTopic matchingTopic = matchingTopics.stream()
                                    .filter(mt -> mt.getStatus().equals(TopicStatus.WAIT))
                                    .filter(mt -> mt.getTopic().getId() <= SUBJECT_LIMIT)
                                    .sorted(Comparator.comparing(MatchingTopic::getSequence))
                                    .findFirst().get();
                            matchingTopic.setStatus(TopicStatus.SELECT);
                            matchingTopicRepository.save(matchingTopic);
                            Long topicId = matchingTopic.getTopic().getId();

                            LocalDateTime time = LocalDateTime.now();

                            Subject subject = topicRepository.findSubjectById(topicId)
                                    .orElseThrow(() -> new BlindCafeException(INVALID_TOPIC));

                            // 메세지 db에 저장
                            User admin = userRepository.findById(0L)
                                    .orElseThrow(() -> new BlindCafeException(NO_USER));
                            Message message = new Message();
                            message.setMatching(matching);
                            message.setUser(admin);
                            message.setContents("벨을 눌러 다양한 대화 토픽을 받아보세요!\n예시를 보여드릴게요.");
                            message.setType(MessageType.DESCRIPTION);
                            message.setCreatedAt(time);
                            Message savedMessage = messageRepository.save(message);

                            Timestamp timestamp = Timestamp.valueOf(time);

                            FirestoreDto firestoreDto = FirestoreDto.builder()
                                    .roomId(matching.getId())
                                    .targetToken(null)
                                    .message(new FirestoreDto.FirestoreMessage(
                                            Long.toString(savedMessage.getId()),
                                            Long.toString(admin.getId()),
                                            admin.getNickname(),
                                            savedMessage.getContents(),
                                            message.getType().getFirestoreType(),
                                            timestamp
                                    ))
                                    .build();
                            firebaseService.insertMessage(firestoreDto);

                            insertTopic(matching, subject.getSubject(), MessageType.TEXT_TOPIC, time);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new BlindCafeException(TOPIC_SERVE_THREAD_ERROR);
                }
            }
        });
        thread.start();
    }

    private void insertTopic(Matching matching, String contents, MessageType messageType, LocalDateTime ldt) {
        // 메세지 db에 저장
        User admin = userRepository.findById(0L)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        Message message = new Message();
        message.setMatching(matching);
        message.setUser(admin);
        message.setContents(contents);
        message.setType(messageType);
        message.setCreatedAt(ldt);
        Message savedMessage = messageRepository.save(message);

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
}