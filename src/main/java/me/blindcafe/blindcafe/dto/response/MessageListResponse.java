package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.Message;
import me.blindcafe.blindcafe.utils.DateTimeUtil;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageListResponse {

    private Page<MessageDetail> messages;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MessageDetail {
        private String messageId;
        private Long userId;
        private String type;
        private String content;
        private LocalDateTime createdAt;

        public static MessageDetail fromEntity(Message message) {
            return MessageDetail.builder()
                    .messageId(message.getId())
                    .userId(Long.parseLong(message.getUserId()))
                    .type(String.valueOf(message.getType().getType()))
                    .content(message.getContent())
                    .createdAt(DateTimeUtil.fromString(message.getCreatedAt()))
                    .build();
        }
    }
}
