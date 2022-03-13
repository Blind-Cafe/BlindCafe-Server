package com.example.BlindCafe.entity;

import com.example.BlindCafe.entity.topic.Topic;
import com.example.BlindCafe.entity.type.status.TopicStatus;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingTopic extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "matching_topic_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) default 'WAIT'")
    private TopicStatus status;
}
