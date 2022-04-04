package me.blindcafe.blindcafe.utils;

import me.blindcafe.blindcafe.domain.type.MessageType;
import me.blindcafe.blindcafe.dto.chat.MessageDto;
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
        return MessageDto.fromAdmin(mid, MessageType.MATCHING, content);
    }

    // 음료수 선택할 때 전송하는 메시지
    public MessageDto selectDrink(Long mid, String username, String drinkName) {
        String content = drinkName + "를 주문한 " + username + "님이세요. 간단한 인사로 반갑게 맞아주세요👋🏻";
        return MessageDto.fromAdmin(mid, MessageType.DRINK, content);
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
        return MessageDto.fromAdmin(mid, MessageType.SUCCESS_EXCHANGE, content);
    }
    
    // 음료수 뱃지 획득 전송 메시지
    public MessageDto takeDrink(Long mid, String username, String drinkName) {
        String content = username + "님이 <" + drinkName + "> 뱃지를 획득하셨습니다.";
        return MessageDto.fromAdmin(mid, MessageType.TAKE_DRINK, content);
    }
    
    // 방 나가기 메시지
    public MessageDto leaveMatching(Long mid, String username, String partnerName, String reason) {
        String content = username + "님이 <" + reason + "> 이유로 방을 나가셨습니다.\n"
                        + "너무 아쉬워하지 마세요. " + partnerName +  "님에게 더 좋은 인연이 찾아올꺼에요.\n"
                        + "그럼, 새로운 인연을 찾으러 가볼까요?";
        return MessageDto.fromAdmin(mid, MessageType.LEAVE, content);
    }

    // 24/48시간 지났을 때 전송하는 메시지
    public MessageDto sendMatchingFunction(Long mid, int day) {
        if (day == 1) {
            String content = "대화 방이 열린 지, 24시간이 지났습니다.\n"
                            + "지금부터 사진을 보내실 수 있어요📸\n"
                            + "첫 사진으로, 즐거웠던 여행 사진을 보내볼까요?";
            return MessageDto.fromAdmin(mid, MessageType.GRANT_IMAGE, content);
        } else {
            String content = "대화 방이 열린 지, 48시간이 지났습니다.\n"
                            + "이제 내 목소리를 녹음해 전송할 수 있어요🎙️\n"
                            + "간단한 인사말 혹은 좋아하는 노래 한 소절을 보내볼까요?";
            return MessageDto.fromAdmin(mid, MessageType.GRANT_VOICE, content);
        }
    }

    // 3일 채팅에서 종료 1시간 전에 전송하는 메시지
    public MessageDto sendEndOfBasicMatching(Long mid) {
        String content = "💗잠시 후 이 대화방은 닫힙니다. 그동안 못다한 말을 해보세요💗";
        return MessageDto.fromAdmin(mid, MessageType.END_OF_ONE_HOUR, content);
    }

    // 72시간 지났을 때(프로필 공개 의사 여부 확인) 전송하는 메시지
    public MessageDto sendExchangeProfile(Long mid) {
        String content = "내 프로필을 전송하고 상대방이 프로필을 받아보세요.\n프로필 교환 성공 시, 이어 대화하실 수 있습니다.";
        return MessageDto.fromAdmin(mid, MessageType.EXCHANGE_PROFILE, content);
    }

    // 7일 채팅에서 종료 1일전에 전송하는 메시지
    public MessageDto sendEndOfContinuousMatching(Long mid) {
        String content = "💗내일 이 대화방은 닫힙니다. 그동안 못다한 말을 해보세요💗";
        return MessageDto.fromAdmin(mid, MessageType.LAST_CHAT, content);
    }
}
