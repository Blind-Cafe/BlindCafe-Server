package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.Message;
import com.example.BlindCafe.domain.RoomLog;
import com.example.BlindCafe.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingListResponse {

    private boolean request;
    private List<MatchingInfo> blind;
    private List<MatchingInfo> bright;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MatchingInfo {
        private Long matchingId;
        private Partner partner;
        private String latestMessage;
        private boolean received;
        private boolean blind;
        private LocalDateTime expiredDt;

        public static MatchingInfo fromEntity(Matching matching, Long userId) {
            User partner = matching.getUserMatchings().stream()
                    .filter(userMatching -> !userMatching.getUser().getId().equals(userId))
                    .findAny()
                    .map(partnerMatching -> partnerMatching.getUser()).orElse(null);

            if (partner != null) {
                MatchingInfo info = new MatchingInfo();
                info.setMatchingId(matching.getId());
                info.setPartner(Partner.fromEntity(partner));
                info.setBlind(!matching.getIsContinuous());
                info.setExpiredDt(matching.getExpiredTime());
            }
            return null;
        }

        public void updateHistory(RoomHistory history) {
            this.latestMessage = history.getLatestMessage();
            this.received = history.isReceived();
        }
    }

    @Setter
    @NoArgsConstructor
    public static class Partner {
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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoomHistory {
        private Long matchingId;
        private String latestMessage;
        private boolean received;
        private String createdAt;

        public static RoomHistory fromEntities(Message message, RoomLog log) {
            RoomHistory history = new RoomHistory();
            history.setMatchingId(message.getMatchingId());
            history.setLatestMessage(message.getContent());
            boolean received = false;
            if (log != null) {
                received = message.getCreatedAt().compareTo(log.getAccessAt()) <= 0 ? true : false;
            }
            history.setReceived(received);
            history.setCreatedAt(message.getCreatedAt());
            return history;
        }
    }
}
