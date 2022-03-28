package com.example.BlindCafe.dto.chat;

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
