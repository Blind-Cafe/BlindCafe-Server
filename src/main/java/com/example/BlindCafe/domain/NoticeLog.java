package com.example.BlindCafe.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.time.LocalDateTime;

@Document(collection = "notice_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeLog {

    @Id
    private String id;

    private Long userId;

    private String accessDt;

    public static NoticeLog create(Long uid, LocalDateTime accessDt) {
        NoticeLog log = new NoticeLog();
        log.setUserId(uid);
        log.setAccessDt(accessDt.toString());
        return log;
    }

    public void update(LocalDateTime accessDt) {
        this.accessDt = accessDt.toString();
    }
}
