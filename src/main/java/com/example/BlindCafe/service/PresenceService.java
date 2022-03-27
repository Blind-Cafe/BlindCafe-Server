package com.example.BlindCafe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, String> redisTemplate;

    private final String SESSION_KEY = "p:session:";
    private final String USER_KEY = "p:user:";
    private final String MATCHING_KEY = "p:matching:";
    private final String DISCONNECT = "off";

    /**
     * 연결
     */
    public void connect(String uid, String sessionId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(USER_KEY + uid, sessionId);
        valueOperations.set(SESSION_KEY + sessionId, uid);
    }

    /**
     * 방 입장
     */
    public void joinRoom(String sessionId, String mid) {
        String uid = getUidBySessionId(sessionId);
        if (uid != null) {
            SetOperations<String, String> setOperations = redisTemplate.opsForSet();
            setOperations.add(MATCHING_KEY + mid, uid);
        }
    }

    /**
     * 방 퇴장
     */
    public void leaveRoom(String sessionId, String mid) {
        String uid = getUidBySessionId(sessionId);
        if (uid != null) {
            SetOperations<String, String> setOperations = redisTemplate.opsForSet();
            setOperations.remove(MATCHING_KEY + mid, uid);
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
     * 접속 유무 확인
     */
    public boolean isConnected(Long userId) {
        String uid = userId.toString();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String sessionId = valueOperations.get(USER_KEY + uid);
        return sessionId != null && !sessionId.equals(DISCONNECT);
    }

    private String getUidBySessionId(String sessionId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String uid = valueOperations.get(SESSION_KEY + sessionId);
        if (uid != null && !uid.equals(DISCONNECT)) return uid;
        else return null;
    }
}
