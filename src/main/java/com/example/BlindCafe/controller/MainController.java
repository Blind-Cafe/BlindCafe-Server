package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.response.HomeResponse;
import com.example.BlindCafe.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.BlindCafe.config.jwt.JwtAuthorizationFilter.UID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainController {

    private final MainService mainService;

    @GetMapping
    public ResponseEntity<HomeResponse> home(
            @RequestHeader(value = UID) String uid
    ) {
        log.info("GET /api/main - UID : {}", UID);
        return ResponseEntity.ok(mainService.home(Long.parseLong(uid)));
    }
}
