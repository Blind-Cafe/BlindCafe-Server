package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.NotificationSetting;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.dto.chat.MessageDto;
import com.example.BlindCafe.dto.request.NotificationSettingRequest;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.utils.FcmUtil;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.BlindCafe.domain.type.Platform.AOS;
import static com.example.BlindCafe.exception.CodeAndMessage.EMPTY_USER;
import static com.example.BlindCafe.exception.CodeAndMessage.INVALID_MESSAGE_TYPE;

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
    private static final Long ALL = 0L;

    private static final int EITHER_FALSE = 0;
    private static final int BOTH_TRUE = 1;
    private static final int EITHER_NULL = 2;

    /**
     * 사용자 ID를 기준으로 매번 DB를 조회하는 건 비효율적
     * 캐시를 사용하면 좋겠지만 현재 스펙상 메모리를 활용해서 사용자의 장비 정보를 관리
     * (ConcurrentHashMap 활용해서 최소한의 동시성 문제 고려)
     * AuthService.login()에서 사용자 로그인 시 장비 값 업데이트
     * 계속 메모리로 관리하는 경우 너무 커질 수 있기 때문에 배치 작업으로 1시간마다 리셋
     * (반복적인 채팅에 대한 효율 향상, 불필요한 메모리 차지 절약)
     */
    // 알림 전체 ON/OFF 설정
    public static ConcurrentHashMap<Long, Boolean> entireNotificationSettingInMemory = new ConcurrentHashMap<>();
    // 특정 채팅방 알림 OFF 명단
    public static ConcurrentHashMap<Long, String> roomNotificationOffInMemory = new ConcurrentHashMap<>();
    // 사용자 디바이스 정보
    public static ConcurrentHashMap<Long, Pair<Platform, String>> deviceInfoInMemory = new ConcurrentHashMap<>();

    /**
     * 채팅 메시지 알림 전송
     */
    public void sendPushMessage(Long userId, MessageDto messageDto) {

        // 전체 또는 채팅방 알림 ON/OFF 여부 확인
        int result = isActivate(userId, messageDto.getMatchingId());
        // 전체 또는 채팅방 중 하나라도 OFF인 경우
        if (result == EITHER_FALSE) return; 
        // 전체 또는 채팅방 중 하나라도 정보를 모르는 경우
        if (result != BOTH_TRUE) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
            NotificationSetting setting = user.getSetting();
            entireNotificationSettingInMemory.put(userId, setting.isAll());
            roomNotificationOffInMemory.put(userId, setting.getInactivateRooms());
            // 다시 검사해서 둘 중 하나라도 OFF인 경우 처리
            if (isActivate(userId, messageDto.getMatchingId()) == EITHER_FALSE) return;
        }

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

        MessageType type = getMessageType(messageDto.getType());

        Message message = fcmUtil.makeMessage(
                deviceInfo.getSecond(), // device token
                fcmUtil.makeTitle(type, messageDto.getSenderName()), // title
                fcmUtil.makeBody(type, messageDto.getContent()), // body
                fcmUtil.makeImage(type, messageDto.getContent()), // image
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

    // 알림 설정 변경
    @Transactional
    public void update(Long userId, NotificationSettingRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        NotificationSetting setting = user.getSetting();

        if (request.getTarget().equals(ALL)) {
            setting.setAll(request.isActive());
            entireNotificationSettingInMemory.put(userId, request.isActive());
            return;
        }

        setting.setRoom(String.valueOf(request.getTarget()), request.isActive());
        roomNotificationOffInMemory.put(userId, setting.getInactivateRooms());
    }

    private int isActivate(Long userId, String mid) {

        boolean status1 = false;
        boolean status2 = false;

        // 메모리에 ON/OFF 설정 있는지 조회
        Boolean entireActivate = entireNotificationSettingInMemory.getOrDefault(userId, null);
        if (entireActivate != null) {
            if (!entireActivate) return EITHER_FALSE;
            status1 = true;
        }

        String offRoomList = roomNotificationOffInMemory.getOrDefault(userId, null);
        if (offRoomList != null) {
            String[] offRooms = offRoomList.split(":");
            for (String offRoom: offRooms) {
                if (offRoom.equals(mid)) return EITHER_FALSE;
            }
            status2 = true;
        }

        if (status1 && status2) return BOTH_TRUE;
        else return EITHER_NULL;
    }

    // 메세지 타입 조회
    private MessageType getMessageType(String type) {
        for (MessageType messageType: MessageType.values()) {
            if (messageType.getType().equals(type)) {
                return messageType;
            }
        }
        throw new BlindCafeException(INVALID_MESSAGE_TYPE);
    }
}
