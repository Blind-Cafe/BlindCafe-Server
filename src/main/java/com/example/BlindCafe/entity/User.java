package com.example.BlindCafe.entity;

import com.example.BlindCafe.entity.type.Platform;
import com.example.BlindCafe.entity.type.Gender;
import com.example.BlindCafe.entity.type.Social;
import com.example.BlindCafe.entity.type.status.UserStatus;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name="social_id", unique=true)
    private String socialId;

    @Enumerated(STRING)
    @Column(length = 10, nullable = false)
    private Social socialType;

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<UserMatching> userMatchings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<InterestOrder> interestOrders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<ProfileImage> profileImages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<UserDrink> userDrinks = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = ALL)
    private List<Report> myReport = new ArrayList<>();

    @OneToMany(mappedBy = "reported", cascade = ALL)
    private List<Report> reported = new ArrayList<>();

    @Embedded
    private Address address;

    private String deviceToken;

    @Enumerated(STRING)
    private Platform platform;

    @Enumerated(STRING)
    private UserStatus status;

    public static User create(
            Social socialType,
            String socialId,
            Platform platform,
            String deviceToken
    ) {
        User user = new User();
        user.setSocialType(socialType);
        user.setSocialId(socialId);
        user.setPlatform(platform);
        user.setDeviceToken(deviceToken);
        user.setStatus(UserStatus.NOT_YET);
        return user;
    }

    public void updateDevice(Platform platform, String deviceToken) {
        this.setPlatform(platform);
        this.setDeviceToken(deviceToken);
    }
}
