package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.topic.Audio;
import com.example.BlindCafe.domain.topic.Image;
import com.example.BlindCafe.domain.topic.Subject;
import com.example.BlindCafe.domain.type.MessageType;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicResponse {
    private Long topicId;
    @Enumerated(EnumType.STRING)
    private MessageType type;
    private String text;
    private String title;
    private String src;
    private LocalDateTime access;

    public static TopicResponse fromSubject(Subject subject, MessageType messageType, LocalDateTime access) {
        return TopicResponse.builder()
                .topicId(subject.getId())
                .type(messageType)
                .text(subject.getSubject())
                .access(access)
                .build();
    }

    public static TopicResponse fromAudio(Audio audio, MessageType messageType, LocalDateTime access) {
        return TopicResponse.builder()
                .topicId(audio.getId())
                .type(messageType)
                .title(audio.getTitle())
                .src(audio.getSrc())
                .access(access)
                .build();
    }

    public static TopicResponse fromImage(Image image, MessageType messageType, LocalDateTime access) {
        return TopicResponse.builder()
                .topicId(image.getId())
                .type(messageType)
                .title(image.getTitle())
                .src(image.getSrc())
                .access(access)
                .build();
    }
}
