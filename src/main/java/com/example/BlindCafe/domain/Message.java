package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.MessageType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "message")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @Column(name = "message_id")
    private String id;

    private String matchingId;

    private String userId;

    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String createdAt;

    public static Message create(String matchingId, String userId, String content, MessageType type) {
        LocalDateTime createdAt = LocalDateTime.now();
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setMatchingId(matchingId);
        message.setUserId(userId);
        message.setContent(content);
        message.setType(type);
        message.setCreatedAt(createdAt.toString());
        return message;
    }
}
