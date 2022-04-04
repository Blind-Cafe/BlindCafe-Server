package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartnerResponse {
    private Long userId;
    private String nickname;
    private String avatar;

    public static PartnerResponse fromEntity(User user) {
        PartnerResponse partnerResponse = new PartnerResponse();
        if (user != null) {
            partnerResponse.setUserId(user.getId());
            partnerResponse.setNickname(user.getNickname());
            partnerResponse.setAvatar(user.getMainAvatar());
        } else {
            partnerResponse.setUserId(0L);
            partnerResponse.setNickname("(알 수 없음)");
        }
        return partnerResponse;
    }
}
