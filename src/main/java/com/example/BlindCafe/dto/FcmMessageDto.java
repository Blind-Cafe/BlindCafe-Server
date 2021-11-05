package com.example.BlindCafe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FcmMessageDto {
    private boolean validate_only;
    private Message message;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String token;
        private Notification notification;
        private FcmData data;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class FcmData {
        private String path;
    }
}
