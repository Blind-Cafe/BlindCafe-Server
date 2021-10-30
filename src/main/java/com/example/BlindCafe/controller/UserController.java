package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.CreateUserInfoDto;
import com.example.BlindCafe.dto.UserHomeDto;
import com.example.BlindCafe.dto.LoginDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.example.BlindCafe.type.Social.APPLE;
import static com.example.BlindCafe.type.Social.KAKAO;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/kakao")
    public LoginDto.Response signupKakao(@Valid @RequestBody LoginDto.Request request) {
        log.info("POST /api/kakao");
        return userService.signin(request, KAKAO);
    }

    @PostMapping("/api/apple")
    public LoginDto.Response signupApple(@Valid @RequestBody LoginDto.Request request) {
        log.info("POST /api/apple");
        return userService.signin(request, APPLE);
    }

    @PostMapping("/api/user")
    public CreateUserInfoDto.Response addUserInfo(
            Authentication authentication,
            @Valid @RequestBody CreateUserInfoDto.Request request
    ) {
        log.info("POST /api/user");
        User user = (User) authentication.getPrincipal();
        return userService.addUserInfo(user, request);
    }

    @GetMapping("/api/user/home")
    public UserHomeDto.Response userHome(Authentication authentication) {
        log.info("GET /api/user/home");
        User user = (User) authentication.getPrincipal();
        return userService.userHome(user.getId());
    }

}
