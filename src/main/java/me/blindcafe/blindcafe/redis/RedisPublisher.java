package me.blindcafe.blindcafe.redis;

import me.blindcafe.blindcafe.dto.chat.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer redisMessageListener;
    private final RedisSubscriber redisSubscriber;
    private Map<String, ChannelTopic> topics;

    private final String MATCHING_TOPIC_PREFIX = "m-";
    private final String USER_TOPIC_PREPIX = "u-";

    @PostConstruct
    private void init() {
        topics = new HashMap<>();
    }

    public void publish(String id, MessageDto message, boolean isMatching) {
        ChannelTopic topic = getTopic(id, isMatching);
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }

    private ChannelTopic getTopic(String id, boolean isMatching) {
        String key;
        if (isMatching) key = MATCHING_TOPIC_PREFIX + id;
        else key = USER_TOPIC_PREPIX + id;

        ChannelTopic topic = topics.get(key);
        if (Objects.isNull(topic)) {
            topic = new ChannelTopic(key);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(key, topic);
        }
        return topic;
    }
}
