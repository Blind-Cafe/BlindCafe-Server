package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.Gender;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.domain.type.ReasonType;
import com.example.BlindCafe.domain.type.Social;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_reason")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomReason extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_reason_id")
    private Long id;

    private Long userId;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender myGender;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender partnerGender;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    private Long matchingId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReasonType type;

    private String reason;

    public static CustomReason create(User user, Long matchingId, Reason reason) {
        CustomReason customReason = new CustomReason();
        customReason.setUserId(user.getId());
        customReason.setAge(user.getAge());
        customReason.setMyGender(user.getMyGender());
        customReason.setPartnerGender(user.getPartnerGender());
        customReason.setPlatform(user.getPlatform());
        customReason.setMatchingId(matchingId);
        customReason.setType(reason.getReasonType());
        customReason.setReason(reason.getText());
        return customReason;
    }

    public static CustomReason create(User user, Reason reason) {
        CustomReason customReason = new CustomReason();
        customReason.setUserId(user.getId());
        customReason.setAge(user.getAge());
        customReason.setMyGender(user.getMyGender());
        customReason.setPartnerGender(user.getPartnerGender());
        customReason.setPlatform(user.getPlatform());
        customReason.setType(reason.getReasonType());
        customReason.setReason(reason.getText());
        return customReason;
    }
}
