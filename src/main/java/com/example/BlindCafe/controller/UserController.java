package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.LoginDto;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/kakao")
    public void signupKakao(@Valid @RequestBody LoginDto.Request request) {
        log.info("POST /api/kakao - request : " + request);

        userService.signinByKakao(request);
    }

}
