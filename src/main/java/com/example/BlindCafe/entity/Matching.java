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

    private LocalDateTime startTime;

    @Column(columnDefinition = "boolean default true", nullable = false)
    private Boolean isValid;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(20) default 'SUCCESS_STAGE_ONE'", nullable = false)
    private MatchingStatus status;
}
