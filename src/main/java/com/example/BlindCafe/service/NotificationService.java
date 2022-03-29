package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.dto.chat.MessageDto;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.utils.FcmUtil;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.BlindCafe.domain.type.Platform.AOS;
import static com.example.BlindCafe.exception.CodeAndMessage.EMPTY_USER;

/**
 * TODO FCM 전송 로그 저장하기
 */

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${fcm.firebase-multicast-message-size}")
    private int MULTICAST_MESSAGE_SIZE;

    private final FcmUtil fcmUtil;

    private final UserRepository userRepository;

    private static final String NOTICE = "N";
    private static final String CHAT = "C";

    /**
     * 사용자 ID를 기준으로 매번 DB를 조회하는 건 비효율적
     * 캐시를 사용하면 좋겠지만 현재 스펙상 메모리를 활용해서 사용자의 장비 정보를 관리
     * (ConcurrentHashMap 활용해서 최소한의 동시성 문제 고려)
     * AuthService.login()에서 사용자 로그인 시 장비 값 업데이트
     * 계속 메모리로 관리하는 경우 너무 커질 수 있기 때문에 배치 작업으로 1시간마다 리셋
     * (반복적인 채팅에 대한 효율 향상, 불필요한 메모리 차지 절약)
     */
    public static ConcurrentHashMap<Long, Pair<Platform, String>> deviceInfoInMemory = new ConcurrentHashMap<>();

    /**
     * 채팅 메시지 알림 전송
     */
    public void sendPushMessage(Long userId, MessageDto messageDto) {
        // 메모리에 사용자 정보 있는지 조회
        Pair<Platform, String> deviceInfo = deviceInfoInMemory.getOrDefault(userId, null);

        if (deviceInfo == null) {
            // 사용자 정보가 없는 경우
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BlindCafeException(EMPTY_USER));

            Platform platform = user.getPlatform();
            String deviceToken = user.getDeviceToken();
            
            Pair<Platform, String> newDeviceInfo = Pair.of(platform, deviceToken);
            // 사용자 장비 정보 갱신
            deviceInfoInMemory.put(userId, newDeviceInfo);
            deviceInfo = newDeviceInfo;
        }

        Message message = fcmUtil.makeMessage(
                deviceInfo.getSecond(), // device token
                fcmUtil.makeTitle(messageDto.getSenderName()), // title
                fcmUtil.makeBody(messageDto.getType(), messageDto.getContent()), // body
                fcmUtil.makeImage(messageDto.getType(), messageDto.getContent()), // image
                deviceInfo.getFirst(), // platform
                fcmUtil.makeCustomData(CHAT, messageDto.getMatchingId()) // custom data
        );
        fcmUtil.sendMessage(message);
    }

    /**
     * 플랫폼 별 공지에 대한 알림 전송
     */
    public void sendPushMessageAtGroupNotice(String title, String content, Platform platform) {

        List<String> tokens = userRepository.findNormalUsersByPlatform(platform.getDescription()).stream()
                .map(User::getDeviceToken)
                .collect(Collectors.toList());

        int size = tokens.size();
        int iteration = size / MULTICAST_MESSAGE_SIZE;
        if (size % MULTICAST_MESSAGE_SIZE != 0) iteration++;

        MulticastMessage message;
        for (int count=0; count<iteration; count++) {
            if (count == iteration-1) {
                message = fcmUtil.makeMessage(
                        tokens.subList(count * MULTICAST_MESSAGE_SIZE, size),
                        title,
                        content,
                        null,
                        AOS,
                        fcmUtil.makeCustomData(NOTICE, "0"));
            } else {
                message = fcmUtil.makeMessage(
                        tokens.subList(count * MULTICAST_MESSAGE_SIZE, (count + 1) * MULTICAST_MESSAGE_SIZE),
                        title,
                        content,
                        null,
                        AOS,
                        fcmUtil.makeCustomData(NOTICE, "0"));
            }
            fcmUtil.sendMessage(message);
        }
    }
}
