package com.example.BlindCafe.dto.chat;

import com.example.BlindCafe.domain.Message;
import com.example.BlindCafe.domain.type.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto {
    private String messageId;
    private String matchingId;
    private String senderId;
    private String senderName;
    private String type;
    private String content;
    private String destination = "0";

    public static MessageDto fromCollection(Message message) {
        return MessageDto.builder()
                .messageId(message.getId())
                .matchingId(message.getMatchingId())
                .senderId(message.getUserId())
                .senderName(message.getUsername())
                .type(message.getType().getType())
                .content(message.getContent())
                .destination("0")
                .build();
    }

    public static MessageDto fromAdmin(Long mid, MessageType type, String content) {
        return MessageDto.builder()
                .matchingId(String.valueOf(mid))
                .senderId("0")
                .senderName("admin")
                .type(type.getType())
                .content(content)
                .destination("0")
                .build();
    }

    public static MessageDto fromFileMessage(FileMessageDto fileMessageDto, String src) {
        return MessageDto.builder()
                .matchingId(fileMessageDto.getMatchingId())
                .senderId(fileMessageDto.getSenderId())
                .senderName(fileMessageDto.getSenderName())
                .type(fileMessageDto.getType())
                .destination("0")
                .content(src)
                .build();
    }
}
