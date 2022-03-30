package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.NotificationSettingRequest;
import com.example.BlindCafe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.BlindCafe.config.SecurityConfig.getUid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 설정 변경
     */
    @PutMapping
    public ResponseEntity<Void> update(
            Authentication authentication,
            @Valid @RequestBody NotificationSettingRequest request
    ) {
        log.info("PUT /api/notification");
        notificationService.update(getUid(authentication), request);
        return ResponseEntity.ok().build();
    }
}
