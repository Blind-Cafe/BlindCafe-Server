package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.Avatar;
import com.example.BlindCafe.domain.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.example.BlindCafe.domain.type.status.CommonStatus.NORMAL;

@Getter
@AllArgsConstructor
public class MatchingListDto {

    private List<MatchingDto> matchings;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class MatchingDto {
        private Long matchingId;
        private Partner partner;
        private String latestMessage;
        private boolean received;
        private String expiryTime;
        private LocalDateTime time;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Partner {
        private Long userId;
        private String profileImage;
        private String nickname;

        public Partner(User user) {
            Avatar avatar = user.getAvatars()
                    .stream().sorted(Comparator.comparing(Avatar::getPriority))
                    .filter(pi -> pi.getStatus().equals(NORMAL))
                    .findFirst()
                    .orElse(null);
            String src = avatar != null ? avatar.getSrc() : null;

            this.userId = user.getId();
            this.profileImage = src;
            this.nickname = user.getNickname();
        }
    }
}
