package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.ExchangeProfileRequest;
import com.example.BlindCafe.dto.request.SelectDrinkRequest;
import com.example.BlindCafe.dto.response.MatchingDetailResponse;
import com.example.BlindCafe.dto.response.MatchingListResponse;
import com.example.BlindCafe.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.BlindCafe.config.SecurityConfig.getUid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 매칭 요청하기
     */
    @PostMapping
    public ResponseEntity<Void> createMatching(Authentication authentication) {
        log.info("POST /api/matching");
        matchingService.createMatching(getUid(authentication));
        return ResponseEntity.ok().build();
    }

    /**
     * 매칭 취소하기
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelMatching(Authentication authentication) {
        log.info("POST /api/matching/cancel");
        matchingService.cancelMatching(getUid(authentication));
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 리스트 조회
     */
    @GetMapping
    public ResponseEntity<MatchingListResponse> getMatchings(Authentication authentication) {
        log.info("GET /api/matching");
        return ResponseEntity.ok(matchingService.getMatchings(getUid(authentication)));
    }

    /**
     * 채팅방 정보 조회
     */
    @GetMapping("/{matchingId}")
    public ResponseEntity<MatchingDetailResponse> getMatching(
            Authentication authentication,
            @PathVariable Long matchingId
    )  {
        log.info("GET /api/matching/{}", matchingId);
        return ResponseEntity.ok(matchingService.getMatching(getUid(authentication), matchingId));
    }

    /**
     * 음료수 선택하기
     */
    @PostMapping("/drink")
    public ResponseEntity<Void> selectDrink(
            Authentication authentication,
            @Valid @RequestBody SelectDrinkRequest request
    ) {
        log.info("POST /api/matching/drink");
        matchingService.selectDrink(getUid(authentication), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 토픽 가져오기
     */
    @GetMapping("/{matchingId}/topic")
    public ResponseEntity<Void> getTopic(@PathVariable Long matchingId) {
        log.info("GET /api/matching/{}/topic", matchingId);
        matchingService.getTopic(matchingId);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 교환 수락/거절하기
     */
    @PostMapping("/exchange")
    public ResponseEntity<Void> exchangeProfile(
            Authentication authentication,
            @Valid @RequestBody ExchangeProfileRequest request
    ) {
        log.info("POST /api/matching/exchange");
        matchingService.exchangeProfile(getUid(authentication), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나가기
     */
    @DeleteMapping("/{matchingId}")
    public ResponseEntity<Void> leaveMatching(
            Authentication authentication,
            @PathVariable Long matchingId,
            @RequestParam(value = "reason", defaultValue = "1") Long reasonId
    ) {
        log.info("DELETE /api/matching/{}", matchingId);
        matchingService.leaveMatching(getUid(authentication), matchingId, reasonId);
        return ResponseEntity.ok().build();
    }
}
