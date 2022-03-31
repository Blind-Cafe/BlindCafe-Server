package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.utils.DateTimeUtil;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "message")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    private String id;

    private String matchingId;

    private String userId;

    private String username;

    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String createdAt;

    public static Message create(String matchingId, String userId, String username, String content, MessageType type) {
        LocalDateTime createdAt = LocalDateTime.now();
        Message message = new Message();
        message.setMatchingId(matchingId);
        message.setUserId(userId);
        message.setUsername(username);
        message.setContent(content);
        message.setType(type);
        message.setCreatedAt(createdAt.format(DateTimeUtil.formatter));
        return message;
    }
}
