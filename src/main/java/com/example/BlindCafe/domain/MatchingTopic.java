package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.topic.Topic;
import lombok.*;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "matching_topic")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingTopic {

    @Id
    @GeneratedValue
    @Column(name = "matching_topic_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    private String all;
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
        topic.setAll(sb.toString());
        topic.setRemain(sb.toString());
        return topic;
    }
}
