package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.status.MatchingStatus;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMatching extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "user_matching_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "drink_id")
    private Drink drink;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "reason_id")
    private Reason reason;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;

    @Enumerated(STRING)
    @Column(length = 20, nullable = false)
    private MatchingStatus status;
}
