package me.blindcafe.blindcafe.redis;

import me.blindcafe.blindcafe.dto.chat.MessageDto;
import me.blindcafe.blindcafe.exception.BlindCafeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import static me.blindcafe.blindcafe.exception.CodeAndMessage.SEND_MESSAGE_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    private static final String MATCHING_TOPIC = "/topic/chat/matching/";
    private static final String USER_TOPIC = "/topic/chat/user/";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = getPublishMessage(message);
            MessageDto messageDto = objectMapper.readValue(publishMessage, MessageDto.class);

            if (messageDto.getDestination().equals("0")) {
                messageTemplate.convertAndSend(MATCHING_TOPIC + messageDto.getMatchingId(), messageDto);
            } else {
                messageTemplate.convertAndSend(USER_TOPIC + messageDto.getDestination(), messageDto);
            }
        } catch (Exception e) {
            throw new BlindCafeException(SEND_MESSAGE_ERROR);
        }
    }

    private String getPublishMessage(Message message) {
        return redisTemplate.getStringSerializer().deserialize(message.getBody());
    }
}