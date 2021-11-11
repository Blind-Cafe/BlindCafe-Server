package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.LoginDto;
import com.example.BlindCafe.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.example.BlindCafe.type.Social.APPLE;
import static com.example.BlindCafe.type.Social.KAKAO;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/kakao")
    public LoginDto.Response signupKakao(@Valid @RequestBody LoginDto.Request request) {
        log.info("POST /api/kakao");
        return authService.signin(request, KAKAO);
    }

    @PostMapping("/api/apple")
    public LoginDto.Response signupApple(@Valid @RequestBody LoginDto.Request request) {
        log.info("POST /api/apple");
        return authService.signin(request, APPLE);
    }
}
