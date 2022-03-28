package com.example.BlindCafe.utils;

import com.example.BlindCafe.domain.type.MessageType;
import com.example.BlindCafe.dto.chat.MessageDto;
import org.springframework.stereotype.Component;

/**
 * 매칭 성공, 음료수 선택 등 매칭 및 진행과 관련된 공통 메시지의 메시지 폼을 만들기 위한 유틸리티
 */

@Component
public class MatchingMessageUtil {

    // 매칭 성공할 때 전송하는 메시지
    public MessageDto successMatching(Long mid, String interestName) {
        String content = "매칭 성공을 축하드립니다🎉\n가벼운 질문 나누며 이야기를 시작해보세요!";
        if (interestName != null)
            content = "<" + interestName + "> 관심사를 선택한 두 분, 매칭 축하드립니다🎉\n선택한 (공통 관심사)에 대해 이야기 나눠보세요!";
        return MessageDto.fromAdmin(mid, MessageType.DESCRIPTION, content);
    }

    // 음료수 선택할 때 전송하는 메시지
    public MessageDto selectDrink(Long mid, String username, String drinkName) {
        String content = drinkName + "를 주문한 " + username + "님이세요. 간단한 인사로 반갑게 맞아주세요👋🏻";
        return MessageDto.fromAdmin(mid, MessageType.DESCRIPTION, content);
    }
    
    // 토픽 제공할 때 전송하는 메시지
    public MessageDto sendTopic(Long mid, MessageType type, String content) {
        return MessageDto.fromAdmin(mid, type, content);
    }
    
    // 프로필 교환 수락 메시지
    public MessageDto exchangeProfile(Long mid, Long userId, String username) {
        return MessageDto.builder()
                .matchingId(String.valueOf(mid))
                .senderId(String.valueOf(userId))
                .senderName(username)
                .type(String.valueOf(MessageType.PROFILE.getType()))
                .content(String.valueOf(userId))
                .destination("0")
                .build();
    }

    // 프로필 교환 성공(7일 채팅)했을 때 전송하는 메시지
    public MessageDto successExchange(Long mid) {
        String content = "축하드립니다. 프로필 교환에 성공하셨습니다🎉🎉\n "
                        + "지금부터 자유롭게 이야기 하실 수 있습니다. "
                        + "못다한 이야기를 어서 나눠보세요!";
        return MessageDto.fromAdmin(mid, MessageType.DESCRIPTION, content);
    }
    
    // 음료수 뱃지 획득 전송 메시지
    public MessageDto takeDrink(Long mid, String username, String drinkName) {
        String content = username + "님이 <" + drinkName + "> 뱃지를 획득하셨습니다.";
        return MessageDto.fromAdmin(mid, MessageType.DESCRIPTION, content);
    }
    
    // 방 나가기 메시지
    public MessageDto leaveMatching(Long mid, String username, String partnerName, String reason) {
        String content = username + "님이 <" + reason + "> 이유로 방을 나가셨습니다.\n"
                        + "너무 아쉬워하지 마세요. " + partnerName +  "님에게 더 좋은 인연이 찾아올꺼에요.\n"
                        + "그럼, 새로운 인연을 찾으러 가볼까요?";
        return MessageDto.fromAdmin(mid, MessageType.DESCRIPTION_NON_PUSH, content);
    }

    // 24시간 지났을 때 전송하는 메시지

    // 48시간 지났을 때 전송하는 메시지

    // 매칭 종료 1시간 전에 전송하는 메시지

    // 72시간 지났을 때(프로필 공개 의사 여부 확인) 전송하는 메시지
}
