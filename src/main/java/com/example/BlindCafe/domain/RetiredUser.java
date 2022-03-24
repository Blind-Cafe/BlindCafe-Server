package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.domain.type.Mbti;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.domain.type.Social;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "retired_user")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RetiredUser extends BaseTimeEntity {

    @Id
    @Column(name = "retired_user_id")
    private Long id;

    @Column(length = 10)
    private String nickname;

    private String phone;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender myGender;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender partnerGender;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Social socialType;

    private String address;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    private String reason;

    public static RetiredUser create(User user, String reason) {
        RetiredUser retiredUser = new RetiredUser();
        retiredUser.setId(user.getId());
        retiredUser.setNickname(user.getNickname());
        retiredUser.setPhone(user.getPhone());
        retiredUser.setAge(user.getAge());
        retiredUser.setMyGender(user.getMyGender());
        retiredUser.setPartnerGender(user.getPartnerGender());
        retiredUser.setSocialType(user.getSocialType());
        retiredUser.setAddress(user.getAddress());
        retiredUser.setPlatform(user.getPlatform());
        retiredUser.setMbti(user.getMbti());
        retiredUser.setReason(reason);
        return retiredUser;
    }
}
