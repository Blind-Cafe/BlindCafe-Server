package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.RoomLog;
import com.example.BlindCafe.repository.RoomLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    private final String SESSION_KEY = "p:session:";
    private final String USER_KEY = "p:user:";
    private final String LOBBY = "0";
    private final String DISCONNECT = "off";

    /**
     * 연결
     */
    public void connect(String uid, String sessionId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(USER_KEY + uid, LOBBY);
        valueOperations.set(SESSION_KEY + sessionId, uid);
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
                roomLog = RoomLog.create(mid, uid, time.toString());
            } else {
                roomLog.update(uid, time.toString());
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
        String uid = valueOperations.get(SESSION_KEY + sessionId);
        if (uid != null) {
            valueOperations.set(USER_KEY + uid, DISCONNECT);
            valueOperations.set(SESSION_KEY + sessionId, DISCONNECT);
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
}
