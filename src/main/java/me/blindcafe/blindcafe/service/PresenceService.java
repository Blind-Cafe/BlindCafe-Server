package me.blindcafe.blindcafe.service;

import me.blindcafe.blindcafe.domain.ConnectLog;
import me.blindcafe.blindcafe.domain.RoomLog;
import me.blindcafe.blindcafe.repository.ConnectLogRepository;
import me.blindcafe.blindcafe.repository.RoomLogRepository;
import me.blindcafe.blindcafe.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PresenceService {

    private final RedisTemplate<String, String> redisTemplate;

    private final RoomLogRepository roomLogRepository;
    private final ConnectLogRepository connectLogRepository;

    private static final String SESSION_KEY = "p:session:";
    private static final String USER_KEY = "p:user:";
    private static final String CONNECTED_COUNT_KEY = "p:connect-count";
    private static final String LOBBY = "0";
    private static final String DISCONNECT = "off";

    /**
     * 연결
     */
    public void connect(String uid, String sessionId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (valueOperations.get(CONNECTED_COUNT_KEY) == null)
            valueOperations.set(CONNECTED_COUNT_KEY, "0");
        else
            valueOperations.set(CONNECTED_COUNT_KEY, String.valueOf(Long.parseLong(valueOperations.get(CONNECTED_COUNT_KEY)) + 1));
        valueOperations.set(USER_KEY + uid, LOBBY);
        valueOperations.set(SESSION_KEY + sessionId, uid);
        connectLogRepository.save(ConnectLog.create(Long.parseLong(uid), LocalDateTime.now(), true));
    }

    /**
     * 방 입장
     */
    public void joinRoom(String sessionId, String mid) {
        String uid = getUidBySessionId(sessionId);
        if (uid != null) {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(USER_KEY + uid, mid);
        }
    }

    /**
     * 방 퇴장
     */
    public void leaveRoom(String sessionId, LocalDateTime time) {
        String uid = getUidBySessionId(sessionId);
        if (uid != null) {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String mid = valueOperations.get(USER_KEY + uid);
            RoomLog roomLog = roomLogRepository.findRoomLogByMatchingId(mid);
            if (Objects.isNull(roomLog)) {
                roomLog = RoomLog.create(mid, uid, time);
            } else {
                roomLog.update(uid, time.format(DateTimeUtil.formatter));
            }
            roomLogRepository.save(roomLog);
            valueOperations.set(USER_KEY + uid, LOBBY);
        }
    }

    /**
     * 종료(연결 해제)
     */
    public void disconnect(String sessionId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (valueOperations.get(CONNECTED_COUNT_KEY) == null)
            valueOperations.set(CONNECTED_COUNT_KEY, "0");
        else
            valueOperations.set(CONNECTED_COUNT_KEY, String.valueOf(Long.parseLong(valueOperations.get(CONNECTED_COUNT_KEY)) - 1));
        String uid = valueOperations.get(SESSION_KEY + sessionId);
        if (uid != null) {
            valueOperations.set(USER_KEY + uid, DISCONNECT);
            valueOperations.set(SESSION_KEY + sessionId, DISCONNECT);
            connectLogRepository.save(ConnectLog.create(Long.parseLong(uid), LocalDateTime.now(), false));
        }
    }

    /**
     * 사용자의 현재 접속해 있는 위치(채팅방 또는 로비)
     */
    public String isCurrentPosition(String uid) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String position = valueOperations.get(USER_KEY + uid);
        if (position == null || position.equals(DISCONNECT))
            return null;
        return position;
    }

    private String getUidBySessionId(String sessionId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String uid = valueOperations.get(SESSION_KEY + sessionId);
        if (uid != null && !uid.equals(DISCONNECT)) return uid;
        else return null;
    }

    /**
     * 현재 접속해 있는 사용자의 수
     */
    public Long getConnectedMemberCount() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (valueOperations.get(CONNECTED_COUNT_KEY) == null)
            return 0L;
        else
            return Long.parseLong(valueOperations.get(CONNECTED_COUNT_KEY));
    }
}
