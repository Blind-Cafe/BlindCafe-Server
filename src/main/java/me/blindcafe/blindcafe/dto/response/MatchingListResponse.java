package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.*;
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
        private PartnerResponse partner;
        private String latestMessage;
        private boolean received;
        private boolean blind;
        private LocalDateTime expiredDt;

        public static MatchingInfo fromEntity(Matching matching, Long userId) {
            User partner = matching.getUserMatchings().stream()
                    .filter(userMatching -> !userMatching.getUser().getId().equals(userId))
                    .findAny()
                    .map(UserMatching::getUser).orElse(null);

            MatchingInfo info = new MatchingInfo();
            info.setMatchingId(matching.getId());
            info.setPartner(PartnerResponse.fromEntity(partner));
            info.setBlind(!matching.getIsContinuous());
            info.setExpiredDt(matching.getExpiredTime());
            return info;
        }

        public void setHistory(RoomHistory history) {
            this.latestMessage = history.getLatestMessage();
            this.received = history.isReceived();
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

        public static RoomHistory fromEntities(Message message, String access) {
            RoomHistory history = new RoomHistory();
            history.setMatchingId(Long.parseLong(message.getMatchingId()));
            String content;
            if (message.getType().getBody() != null)
                content = message.getContent();
            else
                content = message.getType().getBody();
            history.setLatestMessage(content);
            boolean received = false;
            if (access != null) {
                received = message.getCreatedAt().compareTo(access) <= 0;
            }
            history.setReceived(received);
            history.setCreatedAt(message.getCreatedAt());
            return history;
        }
    }
}
