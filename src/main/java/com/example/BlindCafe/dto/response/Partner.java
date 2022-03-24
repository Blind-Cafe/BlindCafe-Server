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
        if (user != null) {
            partner.setUserId(user.getId());
            partner.setNickname(user.getNickname());
            partner.setAvatar(user.getMainAvatar());
        } else {
            partner.setUserId(0L);
            partner.setNickname("(알 수 없음)");
        }
        return partner;
    }
}
