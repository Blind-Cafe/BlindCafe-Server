package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.PushMessageDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final FirebaseCloudMessageService fcm;

    @PostMapping("/fcm")
    public PushMessageDto test(
            @RequestBody PushMessageDto request
    ) {
        fcm.sendMessageTo(
                request.getTargetToken(),
                request.getTitle(),
                request.getBody(),
                request.getPath(),
                0L
        );
        return request;
    }
}
