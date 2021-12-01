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
        private Apns apns;
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
        private String title;
        private String body;
        private String path;
        private String type;
        private String matchingId;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Apns {
        private Payload payload;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private Aps aps;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Aps {
        private String title;
        private String body;
        private String path;
        private String type;
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
