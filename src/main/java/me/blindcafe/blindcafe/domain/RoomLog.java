package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.utils.DateTimeUtil;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "room_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomLog {

    @Id
    private String matchingId;

    private Map<String, String> access;

    public static RoomLog create(String matchingId, String userId, LocalDateTime time) {
        RoomLog log = new RoomLog();
        log.setMatchingId(matchingId);
        Map<String, String> map = new HashMap<>();
        map.put(userId, time.format(DateTimeUtil.formatter));
        log.setAccess(map);
        return log;
    }

    public void update(String userId, String time) {
        this.access.put(userId, time);
    }
}
