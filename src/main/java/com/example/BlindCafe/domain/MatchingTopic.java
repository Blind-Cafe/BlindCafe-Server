package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.topic.Topic;
import com.example.BlindCafe.exception.BlindCafeException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

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
        return topic;
    }

    public Long getTopic() {
        String topicList = this.remain;
        int index = topicList.indexOf(",");
        
        // 더 이상 토픽이 없는 경우
        if (index == -1)
            throw new BlindCafeException(EXCEED_MATCHING_TOPIC);

        this.remain = topicList.substring(index+1);
        return Long.parseLong(topicList.substring(0, index));
    }
}
