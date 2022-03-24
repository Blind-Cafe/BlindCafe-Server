package com.example.BlindCafe.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "room_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomLog {

    @Id
    @Column(name = "room_log_id")
    private String id;

    private Long userId;

    private Long matchingId;

    private String accessAt;

    public static RoomLog create(Long userId, Long matchingId) {
        LocalDateTime now = LocalDateTime.now();
        RoomLog log = new RoomLog();
        log.setId(UUID.randomUUID().toString());
        log.setUserId(userId);
        log.setMatchingId(matchingId);
        log.setAccessAt(now.toString());
        return log;
    }
}
