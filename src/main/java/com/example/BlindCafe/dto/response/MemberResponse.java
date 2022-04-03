package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.domain.type.status.UserStatus;
import com.example.BlindCafe.utils.DateTimeUtil;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String nickname;
    private String phone;
    private int age;
    private String address;
    @Enumerated(EnumType.STRING)
    private Platform platform;
    private String createdAt;
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public static MemberResponse fromEntity(User user) {
        return MemberResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .age(user.getAge())
                .address(user.getAddress())
                .platform(user.getPlatform())
                .createdAt(user.getCreatedAt().format(DateTimeUtil.dateFormatter))
                .status(user.getStatus())
                .build();
    }
}
