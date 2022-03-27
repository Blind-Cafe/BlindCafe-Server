package com.example.BlindCafe.dto.common;

import com.example.BlindCafe.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartnerDto {
    private Long userId;
    private String nickname;
    private String avatar;

    public static PartnerDto fromEntity(User user) {
        PartnerDto partnerDto = new PartnerDto();
        if (user != null) {
            partnerDto.setUserId(user.getId());
            partnerDto.setNickname(user.getNickname());
            partnerDto.setAvatar(user.getMainAvatar());
        } else {
            partnerDto.setUserId(0L);
            partnerDto.setNickname("(알 수 없음)");
        }
        return partnerDto;
    }
}
