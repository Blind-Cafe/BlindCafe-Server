package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.chat.FileMessageDto;
import com.example.BlindCafe.dto.chat.MessageDto;
import com.example.BlindCafe.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 텍스트 메시지 전송
     */
    @MessageMapping("/chat/matching/{mid}")
    public void sendTextMessage(@DestinationVariable String mid, MessageDto message) {
        chatService.publish(mid, message);
    }

    /**
     * 이미지, 비디오, 오디오 파일 전송
     */
    @PostMapping("/api/chat/matching/{mid}")
    public void sendFileMessage(
            @PathVariable(value = "mid") String mid,
            @RequestParam FileMessageDto fileMessage
    ) {
        MessageDto message = chatService.upload(mid, fileMessage);
        chatService.publish(mid, message);
    }

    /**
     * 메시지 조회
     */
}
