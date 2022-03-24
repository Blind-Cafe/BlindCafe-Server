package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Partner {
    private Long userId;
    private String nickname;
    private String avatar;

    public static Partner fromEntity(User user) {
        Partner partner = new Partner();
        partner.setUserId(user.getId());
        partner.setNickname(user.getNickname());
        partner.setAvatar(user.getMainAvatar());
        return partner;
    }
}
