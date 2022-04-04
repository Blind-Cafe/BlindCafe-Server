package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.utils.DateTimeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.time.LocalDateTime;

@Document(collection = "connect_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConnectLog {

    @Id
    private String id;

    private Long userId;

    private String accessDay;

    private String accessTime;

    private boolean connect;

    public static ConnectLog create(Long uid, LocalDateTime time, boolean connect) {
        ConnectLog log = new ConnectLog();
        log.setUserId(uid);
        log.setAccessDay(time.format(DateTimeUtil.dateFormatter));
        log.setAccessTime(time.format(DateTimeUtil.timeFormatter));
        log.setConnect(connect);
        return log;
    }
}
