package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.topic.Topic;
import com.example.BlindCafe.exception.BlindCafeException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.example.BlindCafe.exception.CodeAndMessage.ALREADY_SEND_TOPIC;
import static com.example.BlindCafe.exception.CodeAndMessage.EXCEED_MATCHING_TOPIC;

@Entity
@Table(name = "matching_topic")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_topic_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "topic", fetch = FetchType.LAZY)
    private Matching matching;

    private String entire;
    private String remain;
    private String latest;

    private LocalDateTime access;

    public static MatchingTopic create(List<Topic> topics) {
        MatchingTopic topic = new MatchingTopic();
        Collections.shuffle(topics);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<topics.size(); i++) {
            sb.append(topics.get(i).toString());
            if (i != topics.size()-1)
                sb.append(",");
        }
        topic.setEntire(sb.toString());
        topic.setRemain(sb.toString());
        topic.setLatest(null);
        topic.setAccess(LocalDateTime.now());
        return topic;
    }

    public Long getTopic() {
        LocalDateTime now = LocalDateTime.now();
        String topicList = this.remain;
        int index = topicList.indexOf(",");

        // 더 이상 토픽이 없는 경우
        if (index == -1)
            throw new BlindCafeException(EXCEED_MATCHING_TOPIC);

        // 최근에 토픽을 조회한 경우
        if (this.getAccess().plusMinutes(5L).isAfter(now))
            throw new BlindCafeException(ALREADY_SEND_TOPIC);

        this.remain = topicList.substring(index+1);
        this.latest = topicList.substring(0, index);
        this.access = now;

        return this.getLatestTopic();
    }

    public Long getLatestTopic() {
        if (this.latest != null) return Long.parseLong(this.latest);
        else return null;
    }
}
