package com.example.BlindCafe.dto.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private String matchingId;
    private String sender;
    private String senderName;
    private String type;
    private String content;
}
