package com.example.BlindCafe.entity;

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

    @Column(length = 10)
    private String nickname;

    private int age;

    @Enumerated(STRING)
    @Column(length = 10)
    private Gender myGender;

    @Enumerated(STRING)
    @Column(length = 10)
    private Gender partnerGender;

    @Column(name="social_id" , length = 100, unique=true)
    private String socialId;

    @Enumerated(STRING)
    @Column(length = 10, nullable = false)
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
    private List<UserDrink> userDrinks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<Report> myReport = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<Reported> reported = new ArrayList<>();

    @Embedded
    private Address address;

    private String deviceId;

    @Enumerated(STRING)
    private UserStatus status;
}
