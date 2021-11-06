package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.CreateUserInfoDto;
import com.example.BlindCafe.dto.UserDetailDto;
import com.example.BlindCafe.dto.UserHomeDto;
import com.example.BlindCafe.dto.LoginDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.BlindCafe.config.SecurityConfig.getUserId;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserDetailDto getUserDetail(Authentication authentication) {
        log.info("GET /api/user");
        return userService.getUserDetail(getUserId(authentication));
    }

    @PostMapping
    public CreateUserInfoDto.Response addUserInfo(
            Authentication authentication,
            @Valid @RequestBody CreateUserInfoDto.Request request
    ) {
        log.info("POST /api/user");
        User user = (User) authentication.getPrincipal();
        return userService.addUserInfo(user, request);
    }

    @GetMapping("home")
    public UserHomeDto.Response userHome(Authentication authentication) {
        log.info("GET /api/user/home");
        return userService.userHome(getUserId(authentication));
    }
}
