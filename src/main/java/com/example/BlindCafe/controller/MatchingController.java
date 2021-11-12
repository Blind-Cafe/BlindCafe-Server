package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.*;
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

    /**
     * 내 테이블 조회 - 프로필 교환을 완료한 상대방 목록 조회
     */
    @GetMapping
    public List<MatchingDto> getMatchings(Authentication authentication) {
        log.info("GET /api/matching");
        return matchingService.getMatchings(getUserId(authentication));
    }

    /**
     * 매칭 요청하기
     */
    @PostMapping
    public CreateMatchingDto.Response createMatching(Authentication authentication) {
        log.info("POST /api/matching");
        return matchingService.createMatching(getUserId(authentication));
    }

    /**
     * 채팅방 나가기
     */
    @DeleteMapping("{matchingId}")
    public DeleteMatchingDto deleteMatching(
            Authentication authentication,
            @PathVariable Long matchingId,
            @RequestParam(value = "reason") Long reasonNum
    ) {
        log.info("DELETE /api/matching/{}", matchingId);
        return matchingService.deleteMatching(getUserId(authentication), matchingId, reasonNum);
    }


    /**
     * 음료수 설정하기
     */
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
