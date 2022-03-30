package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.chat.FileMessageDto;
import com.example.BlindCafe.dto.chat.MessageDto;
import com.example.BlindCafe.dto.response.MessageListResponse;
import com.example.BlindCafe.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 텍스트 메시지 전송
     */
    @MessageMapping("/chat/message")
    public void sendTextMessage(MessageDto message) {
        chatService.publish(message.getMatchingId(), message);
    }

    /**
     * 이미지, 비디오, 오디오 파일 전송
     */
    @PostMapping("/api/chat/matching/{mid}")
    public ResponseEntity<Void> sendFileMessage(
            @PathVariable(value = "mid") String mid,
            @RequestParam String matchingId,
            @RequestParam String senderId,
            @RequestParam String senderName,
            @RequestParam String type,
            @RequestParam MultipartFile file
    ) {
        FileMessageDto fileMessage = new FileMessageDto(matchingId, senderId, senderName, type, file);
        MessageDto message = chatService.upload(mid, fileMessage);
        chatService.publish(mid, message);
        return ResponseEntity.ok().build();
    }

    /**
     * 메시지 조회
     */
    @GetMapping("/api/chat/matching/{mid}")
    public ResponseEntity<MessageListResponse> getMessages(
            @PathVariable(value = "mid") String mid,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(chatService.getMessages(mid, page, size));
    }
}
