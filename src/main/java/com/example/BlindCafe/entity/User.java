package com.example.BlindCafe.entity;

import com.example.BlindCafe.entity.type.Platform;
import com.example.BlindCafe.entity.type.Gender;
import com.example.BlindCafe.entity.type.Social;
import com.example.BlindCafe.entity.type.status.CommonStatus;
import com.example.BlindCafe.entity.type.status.UserStatus;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "user")
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

    private String email;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender myGender;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender partnerGender;

    @Column(name="social_id", unique = true)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Social socialType;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserMatching> matchings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserInterest> interests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<InterestOrder> interestOrders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Avatar> avatars = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserDrink> drinks = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    private List<Report> myReport = new ArrayList<>();

    @OneToMany(mappedBy = "reported", cascade = CascadeType.ALL)
    private List<Report> reported = new ArrayList<>();

    @Embedded
    private Address address;

    private String deviceToken;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public List<Avatar> getAvatars() {
        return this.avatars.stream()
                .filter(avatar -> avatar.getStatus().equals(CommonStatus.NORMAL))
                .sorted(Comparator.comparing(Avatar::getSequence))
                .collect(Collectors.toList());
    }

    public List<UserInterest> getInterests() {
        return this.interests.stream()
                .filter(UserInterest::isActive)
                .collect(Collectors.toList());
    }

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

    // 로그인 시 장비 정보 갱신
    public void updateDevice(Platform platform, String deviceToken) {
        this.setPlatform(platform);
        this.setDeviceToken(deviceToken);
    }

    // 사용자 추가 정보 입력 받기 (온보딩)
    public void addRequiredInfo(
            int age,
            Gender myGender,
            String email,
            String nickname,
            Gender partnerGender
    ) {
        this.setAge(age);
        this.setMyGender(myGender);
        this.setEmail(email);
        this.setNickname(nickname);
        this.setPartnerGender(partnerGender);
        this.setStatus(UserStatus.NORMAL);
    }

    // 메인 프로필 이미지 1장 가져오기
    public String getMainAvatar() {
        return this.getAvatars().stream().findFirst().orElse(null).getSrc();
    }

    // 모든 프로필 이미지 가져오기
    public List<String> getCurrentAvatars() {
        List<Avatar> avatars = this.getAvatars();
        String[] currentAvatars = new String[3];
        avatars.forEach(a -> currentAvatars[a.getSequence()-1] = a.getSrc());
        return Arrays.asList(currentAvatars);
    }

    // 사용자 메인 관심사 가져오기
    public List<Interest> getMainInterests() {
        return this.getInterests().stream()
                .map(UserInterest::getInterest)
                .filter(Interest::getIsMain)
                .collect(Collectors.toList());
    }
    
    // 사용자 세부 관심사 가져오기
    public List<Interest> getSubInterests(Long mainInterestId) {
        return this.getInterests().stream()
                .map(UserInterest::getInterest)
                .filter(i -> !i.getIsMain())
                .filter(i -> i.getMain().getId().equals(mainInterestId))
                .collect(Collectors.toList());
    }

    // 사용자 관심사 수정하기
    public void updateInterest(List<Interest> interests) {
        this.getInterests().forEach(userInterest -> userInterest.remove());
        interests.forEach(interest -> UserInterest.create(this, interest));
    }
}
