package com.example.BlindCafe.utils;

import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.exception.BlindCafeException;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.BlindCafe.domain.type.MessageType.*;
import static com.example.BlindCafe.exception.CodeAndMessage.FIREBASE_BUILD_MESSAGE_ERROR;
import static com.example.BlindCafe.exception.CodeAndMessage.FIREBASE_SEND_MESSAGE_ERROR;

@Component
@RequiredArgsConstructor
public class FcmUtil {

    private final FirebaseMessaging instance;

    private static final String TYPE = "type";
    private static final String ROOM = "room";

    // 단일 메시지 전송
    public String sendMessage(Message message) {
        try {
            return instance.send(message);
        } catch (Exception e) {
            throw new BlindCafeException(FIREBASE_SEND_MESSAGE_ERROR);
        }
    }

    // 일괄 메시지 전송
    public BatchResponse sendMessage(MulticastMessage message) {
        try {
            return instance.sendMulticast(message);
        } catch (Exception e) {
            throw new BlindCafeException(FIREBASE_SEND_MESSAGE_ERROR);
        }
    }

    // 단일 전송 메시지 만들기 - 채팅 활용
    public Message makeMessage(String targetToken, String title, String body, String image, Platform platform, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage(image).build();

            Message.Builder messageBuilder = Message.builder();
            messageBuilder.setToken(targetToken);
            messageBuilder.setNotification(notification);

            // 플랫폼 별 커스텀 데이터 설정
            if (platform.equals(Platform.AOS)) messageBuilder.setAndroidConfig(makeAndroidConfig(data));
            else messageBuilder.setApnsConfig(makeApnsConfig(data));

            return messageBuilder.build();
        } catch (Exception e) {
            throw new BlindCafeException(FIREBASE_BUILD_MESSAGE_ERROR);
        }
    }

    // 일괄 전송 메시지 만들기 - 전체 공지 활용
    public MulticastMessage makeMessage(List<String> targetTokens, String title, String body, String image, Platform platform, Map<String, String> data) {
        try {
            Notification notification = Notification
                    .builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage(image).build();

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder();
            messageBuilder.addAllTokens(targetTokens);
            messageBuilder.setNotification(notification);

            // 플랫폼 별 커스텀 데이터 설정
            if (platform.equals(Platform.AOS)) messageBuilder.setAndroidConfig(makeAndroidConfig(data));
            else messageBuilder.setApnsConfig(makeApnsConfig(data));

            return messageBuilder.build();
        } catch (Exception e) {
            throw new BlindCafeException(FIREBASE_BUILD_MESSAGE_ERROR);
        }
    }

    // 제목 만들기
    public String makeTitle(String username) {
        return username;
    }

    // 바디 만들기
    public String makeBody(String type, String content) {
        for (MessageType messageType: MessageType.values()) {
            if (messageType.getType().equals(type)) {
                if (messageType.getBody() != null) return messageType.getBody();
                else return content;
            }
        }
        throw new BlindCafeException(FIREBASE_BUILD_MESSAGE_ERROR);
    }

    // 미리보기 이미지 만들기
    public String makeImage(String type, String content) {
        if (IMAGE.getType().equals(type)) return content;
        else return null;
    }

    // custom data 만들기
    public Map<String, String> makeCustomData(String type, String matchingId) {
        Map<String, String> customData = new HashMap<>();
        customData.put(TYPE, type);
        customData.put(ROOM, matchingId);
        return customData;
    }

    // AOS 커스텀 데이터 설정
    private AndroidConfig makeAndroidConfig(Map<String, String> data) {
        AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder();
        androidConfigBuilder.putAllData(data);
        return androidConfigBuilder.build();
    }

    // iOS(APN) 커스텀 데이터 설정
    private ApnsConfig makeApnsConfig(Map<String, String> data) {
        ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
        Aps.Builder apsBuilder = Aps.builder();
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, String> entry: data.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Optional.of(map).ifPresent(apsBuilder::putAllCustomData);
        apnsConfigBuilder.setAps(apsBuilder.build());
        Optional.of(map).ifPresent(apnsConfigBuilder::putAllCustomData);
        return apnsConfigBuilder.build();
    }
}
