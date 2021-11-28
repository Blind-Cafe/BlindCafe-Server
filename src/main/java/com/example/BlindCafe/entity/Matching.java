package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.status.MatchingStatus;
import lombok.*;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Matching extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "matching_id")
    private Long id;

    @OneToMany(mappedBy = "matching", cascade = ALL)
    private List<UserMatching> userMatchings = new ArrayList<>();

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    @OneToMany(mappedBy = "matching", cascade = ALL)
    private List<MatchingTopic> topics = new ArrayList<>();

    @OneToMany(mappedBy = "matching", cascade = ALL)
    private List<Message> messages = new ArrayList<>();

    private LocalDateTime startTime;
    private LocalDateTime expiryTime;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isContinuous;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(20) default 'MATCHING'", nullable = false)
    private MatchingStatus status;
}
