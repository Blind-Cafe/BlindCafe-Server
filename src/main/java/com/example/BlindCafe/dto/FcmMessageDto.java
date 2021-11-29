package com.example.BlindCafe.dto;

import lombok.*;

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
        private String matchingId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String title;
        private String body;
        private String image;
        private String path;
        private Long matchingId;
    }
}
