package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.CreateMatchingDto;
import com.example.BlindCafe.dto.DrinkDto;
import com.example.BlindCafe.dto.MatchingDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.example.BlindCafe.config.SecurityConfig.getUserId;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping
    public List<MatchingDto> getMatchings(Authentication authentication) {
        log.info("GET /api/matching");
        return matchingService.getMatchings(getUserId(authentication));
    }

    @PostMapping
    public CreateMatchingDto.Response createMatching(Authentication authentication) {
        log.info("POST /api/matching");
        return matchingService.createMatching(getUserId(authentication));
    }

    @PostMapping("{matchingId}/drink")
    public DrinkDto.Response setDrink(
            Authentication authentication,
            @PathVariable Long matchingId,
            @Valid @RequestBody DrinkDto.Request request
    ) {
        log.info("POST /api/matching/{}/drink", matchingId);
        return matchingService.setDrink(getUserId(authentication), matchingId, request);
    }
}
