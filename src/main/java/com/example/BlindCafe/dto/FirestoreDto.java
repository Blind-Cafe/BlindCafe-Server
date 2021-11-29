package com.example.BlindCafe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
@Builder
public class FirestoreDto {
    private Long roomId;
    private String targetToken;
    private FirestoreMessage message;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class FirestoreMessage {
        private String id;
        private String senderUid;
        private String senderName;
        private String contents;
        private int type;
        private Timestamp timestamp;
    }
}
