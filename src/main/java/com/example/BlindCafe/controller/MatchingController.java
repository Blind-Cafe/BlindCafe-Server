package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.CreateMatchingDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping
    public CreateMatchingDto.Response createMatching(Authentication authentication) {
        log.info("POST /api/matching");
        User user = (User) authentication.getPrincipal();
        return matchingService.createMatching(user);
    }
}
