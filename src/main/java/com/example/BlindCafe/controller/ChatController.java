package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.CreateMatchingDto;
import com.example.BlindCafe.dto.MessageDto;
import com.example.BlindCafe.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.BlindCafe.config.SecurityConfig.getUserId;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅 메세지 전송
     */
    @PostMapping("{matchingId}")
    public ResponseEntity<Void> sendMessage(
            Authentication authentication,
            @PathVariable Long matchingId,
            @Valid @RequestBody MessageDto request
    ) {
        log.info("POST /api/chat/{}", matchingId);
        chatService.sendMessage(getUserId(authentication), matchingId, request);
        return ResponseEntity.ok().build();
    }
}
