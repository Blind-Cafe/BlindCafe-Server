package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.Mbti;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.domain.type.Social;
import com.example.BlindCafe.domain.type.status.CommonStatus;
import com.example.BlindCafe.domain.type.status.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.*;
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

    @Column(unique = true)
    private String phone;

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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "matching_history_id")
    private MatchingHistory matchingHistory;

    @Embedded
    private Address address;

    private String deviceToken;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    @Column(columnDefinition = "TEXT")
    private String voice;

    private boolean admin;

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

    public String getAddress() {
        return this.address != null ? this.address.toString() : null;
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
        user.setAdmin(false);
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
            String phone,
            String nickname,
            Gender partnerGender
    ) {
        this.setAge(age);
        this.setMyGender(myGender);
        this.setPhone(phone);
        this.setNickname(nickname);
        this.setPartnerGender(partnerGender);
        this.setStatus(UserStatus.NORMAL);
    }

    // 메인 프로필 이미지 1장 가져오기
    public String getMainAvatar() {
        return Objects.requireNonNull(this.getAvatars().stream().findFirst().orElse(null)).getSrc();
    }

    // 모든 프로필 이미지 가져오기
    public List<String> getCurrentAvatars() {
        List<Avatar> avatars = this.getAvatars();
        String[] currentAvatars = new String[3];
        avatars.forEach(a -> currentAvatars[a.getSequence()-1] = a.getSrc());
        return Arrays.asList(currentAvatars);
    }

    // 프로필 이미지 업로드/수정하기
    public void updateAvatar(String src, int sequence) {
        this.deleteAvatar(sequence);
        Avatar avatar = Avatar.create(this, src, sequence);
        this.avatars.add(avatar);
    }

    // 프로필 이미지 삭제하기
    public void deleteAvatar(int sequence) {
        Optional<Avatar> deleteAvatar = this.getAvatars().stream()
                .filter(a -> a.getSequence() == sequence)
                .findAny();
        deleteAvatar.ifPresent(Avatar::remove);
    }

    // 사용자 메인 관심사 가져오기
    public List<Interest> getMainInterests() {
        return this.getInterests().stream()
                .map(UserInterest::getInterest)
                .filter(Interest::getIsMain)
                .collect(Collectors.toList());
    }

    // 사용자 관심사 수정하기
    public void updateInterest(List<Interest> interests) {
        this.getInterests().forEach(UserInterest::remove);
        interests.forEach(interest -> {
            UserInterest userInterest = UserInterest.create(this, interest);
            this.getInterests().add(userInterest);
        });
    }

    // 사용자 프로필 수정하기
    public void updateProfile(Address address, Gender partnerGender, Mbti mbti) {
        this.setAddress(address);
        this.setPartnerGender(partnerGender);
        this.setMbti(mbti);
    }
    
    // 사용자 목소리 설정하기
    public void updateVoice(String voice) {
        this.setVoice(voice);
    }

    // 사용자 목소리 삭제하기
    public void deleteVoice() {
        this.setVoice(null);
    }
    
    // 티켓 소비
    public void consumeTicket() {
        this.ticket.consume();
    }

    // 티켓 복구
    public void restoreTicket() {
        this.ticket.restore();
    }

    // 매칭 히스토리 업데이트
    public void updateMatchingHistory(Long partnerId) {
        this.matchingHistory.update(partnerId);
    }
}
