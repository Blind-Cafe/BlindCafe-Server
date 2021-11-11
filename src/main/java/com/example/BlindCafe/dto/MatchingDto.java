package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.Matching;
import com.example.BlindCafe.entity.ProfileImage;
import com.example.BlindCafe.entity.User;
import lombok.*;

import java.util.Comparator;

import static com.example.BlindCafe.type.status.CommonStatus.NORMAL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchingDto {

    private Long matchingId;
    private Partner partner;
    private String latestMessage;
    private boolean isReceived;
    private Long expiryDay;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Partner {
        private Long userId;
        private String profileImage;
        private String nickname;

        public Partner(User user) {
            ProfileImage profileImage = user.getProfileImages()
                    .stream().sorted(Comparator.comparing(ProfileImage::getPriority))
                    .filter(pi -> pi.getStatus().equals(NORMAL))
                    .findFirst()
                    .orElse(null);
            String src = profileImage != null ? profileImage.getSrc() : null;

            this.userId = user.getId();
            this.profileImage = src;
            this.nickname = user.getNickname();
        }
    }
}
