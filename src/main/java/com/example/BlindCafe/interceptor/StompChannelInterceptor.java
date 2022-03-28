package com.example.BlindCafe.interceptor;

import com.example.BlindCafe.config.jwt.JwtUtils;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.example.BlindCafe.exception.CodeAndMessage.FORBIDDEN_AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final PresenceService presenceService;

    private final String TOKEN = "token";
    private final String MATCHING = "matching";

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        LocalDateTime now = LocalDateTime.now();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        switch (Objects.requireNonNull(accessor.getCommand())) {
            case CONNECT: // WebSocket 연결, Header 검증
                // 최초 연결 시 Header 검증
                String uid = JwtUtils.getUsedId(getHeaderValue(accessor, TOKEN));
                // 접속한 유저 정보 저장
                presenceService.connect(uid, sessionId);
                break;

            case SUBSCRIBE: // 채팅방 구독 및 본인 토픽 구독
                if (destination == null)
                    break;

                String topic = getTopic(destination);
                // 채팅방 구독
                if (topic != null) presenceService.joinRoom(sessionId, topic);
                break;

            case UNSUBSCRIBE: // 채팅방 구독 취소
                String mid = getHeaderValue(accessor, MATCHING);
                // 채팅방 구독 취소
                if (mid != null) presenceService.leaveRoom(sessionId, now);
                break;
                
            case DISCONNECT: // WebSocket 연결 해제
                presenceService.disconnect(sessionId);
                break;
        }
        return message;
    }

    private String getHeaderValue(StompHeaderAccessor accessor, String headerName) {
        try {
            return Objects.requireNonNull(accessor.getFirstNativeHeader(headerName));
        } catch (Exception e) {
            throw new BlindCafeException(FORBIDDEN_AUTHORIZATION);
        }
    }

    // destination : /queue/chat/matching/1
    private String getTopic(String destination) {
        String[] split = destination.split("/");
        if (split[3].equals(MATCHING)) {
            return split[4];
        }
        return null;
    }
}
