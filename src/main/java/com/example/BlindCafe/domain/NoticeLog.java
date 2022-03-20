package com.example.BlindCafe.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO
 * Mogno로 설정하기
 */

@Entity
@Table(name = "notice_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    private Long userId;

    private LocalDateTime accessDt;

    public static NoticeLog create(Long uid, LocalDateTime accessDt) {
        NoticeLog log = new NoticeLog();
        log.setUserId(uid);
        log.setAccessDt(accessDt);
        return log;
    }

    public void update(LocalDateTime accessDt) {
        this.setAccessDt(accessDt);
    }
}