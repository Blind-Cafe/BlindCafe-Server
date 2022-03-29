package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.Matching;
import com.example.BlindCafe.domain.Message;
import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.dto.chat.FileMessageDto;
import com.example.BlindCafe.dto.chat.MessageDto;
import com.example.BlindCafe.dto.response.MessageListResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.redis.RedisPublisher;
import com.example.BlindCafe.repository.MatchingRepository;
import com.example.BlindCafe.repository.MessageRepository;
import com.example.BlindCafe.utils.AwsS3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final PresenceService presenceService;
    private final NotificationService notificationService;
    private final RedisPublisher redisPublisher;

    private final MessageRepository messageRepository;
    private final MatchingRepository matchingRepository;

    private final AwsS3Util awsS3Util;

    /**
     * 텍스트 메시지 전송
     */
    @Transactional
    public void publish(String mid, MessageDto message) {
        String uid = message.getSenderId();
        boolean isNotification = isNotification(message.getType());

        // 메세지 저장
        Message newMessage = Message.create(mid, uid, message.getSenderName(), message.getContent(), getSavedType(message.getType()));
        newMessage = messageRepository.save(newMessage);
        MessageDto savedMessage = MessageDto.fromCollection(newMessage);
        message.setMessageId(savedMessage.getMessageId());

        // 메시지 퍼블리싱
        redisPublisher.publish(mid, savedMessage, true);

        // 채팅방 외부에 있을 경우 알림을 받을 필요 없는 메시지 (ex. 방 나가기)
        if (!isNotification)
            return;

        // 방에 어떤 사용자가 있는지 확인
        Matching matching = matchingRepository.findValidMatchingById(Long.parseLong(mid))
                .orElseThrow(() -> new BlindCafeException(EMPTY_MATCHING));

        List<String> targets = matching.getUserIds();

        // 메시지를 만든 사람과 전송하는 사람이 같은 경우 타켓에서 삭제 (관리자가 전송하는 경우 다를 수 있음)
        if (!uid.equals("0"))
            targets.remove(uid);

        // 채팅을 수신받을 사용자가 더 이상 없는 경우
        if (targets.size() < 1) return;

        // 메시지를 받아야 하는 사용자들이 접속해있는지 확인 후
        // 접속 유무에 따라 채팅방 리스트 정렬을 위한 퍼블리싱 또는 알림 전송
        for (String target: targets) {
            // 사용자가 접속해있는지 확인
            String currentPosition = presenceService.isCurrentPosition(target);

            // 접속해있지 않다면 푸시 알림 전송
            if (currentPosition == null) {
                notificationService.sendPushMessage(Long.parseLong(target), message);
            } else {
                // 접속해있지만 채팅방에 없는 경우 사용자한테 직접 퍼블리싱
                if (!mid.equals(currentPosition)) {
                    message.setDestination(target);
                    redisPublisher.publish(uid, savedMessage, false);
                }
            }
        }
    }

    /**
     * 이미지, 비디오, 오디오 파일 업로드
     */
    public MessageDto upload(String mid, FileMessageDto message) {
        String src = awsS3Util.uploadFileFromMessage(message.getFile(), mid);
        return MessageDto.fromFileMessage(message, src);
    }

    /**
     * 메시지 조회
     */
    public MessageListResponse getMessages(String mid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> pages = messageRepository.findByMatchingId(mid, pageable);
        return new MessageListResponse(pages.map(MessageListResponse.MessageDetail::fromEntity));
    }

    // 실제 메시지 타입 조회
    private boolean isNotification(String type) {
        for (MessageType messageType: MessageType.values()) {
            if (messageType.getType().equals(type)) {
                return messageType.isNotification();
            }
        }
        throw new BlindCafeException(INVALID_MESSAGE_TYPE);
    }
    
    // 저장할 메시지 타입 조회
    private MessageType getSavedType(String type) {
        for (MessageType messageType: MessageType.values()) {
            if (messageType.isInChat() && messageType.getType().equals(type)) {
                return messageType;
            }
        }
        return MessageType.TEXT;
    }
}
