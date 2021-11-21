package com.example.BlindCafe.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicDto {

    private String type;
    private SubjectDto text;
    private ObjectDto image;
    private ObjectDto audio;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class SubjectDto {
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ObjectDto {
        private String src;
        private String answer;
    }
}
