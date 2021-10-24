package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.AgeRange;
import com.example.BlindCafe.type.Gender;
import com.example.BlindCafe.type.Social;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    private String nickname;

    @Enumerated(STRING)
    private AgeRange ageRange;

    @Enumerated(STRING)
    private Gender myGender;

    @Enumerated(STRING)
    private Gender partnerGender;

    @Column(name="social_id" , unique=true)
    private String socialId;

    @Enumerated(STRING)
    private Social socialType;

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<UserMatching> userMatchings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<InterestOrder> interestOrders = new ArrayList<>();

    /*
     * @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
     * private List<UserInterest> userInterests = new ArrayList<>();
     */

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<ProfileImage> profileImages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<UserBadge> userBadges = new ArrayList<>();

    @OneToMany(mappedBy = "plaintiff", cascade = ALL)
    private List<Report> myReport = new ArrayList<>();

    @Embedded
    private Address address;

    @Enumerated(STRING)
    private UserStatus status;
}
