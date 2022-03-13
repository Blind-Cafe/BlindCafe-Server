package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.LoginRequest;
import com.example.BlindCafe.dto.request.RefreshTokenRequest;
import com.example.BlindCafe.dto.response.LoginResponse;
import com.example.BlindCafe.dto.response.RefreshTokenResponse;
import com.example.BlindCafe.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 소셜 로그인
     * @return
     * HttpStatus.OK : 로그인
     * HttpStatus.CREATED : 회원가입
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login");
        Pair<HttpStatus, LoginResponse> result = authService.login(request);
        HttpStatus status = result.getFirst();
        LoginResponse response = result.getSecond();
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("POST /api/auth/refresh");
        return ResponseEntity.ok(authService.refresh(request));
    }
}
