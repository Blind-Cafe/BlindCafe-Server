package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.ExchangeProfileRequest;
import com.example.BlindCafe.dto.request.OpenProfileRequest;
import com.example.BlindCafe.dto.request.SelectDrinkRequest;
import com.example.BlindCafe.dto.response.MatchingDetailResponse;
import com.example.BlindCafe.dto.response.MatchingListResponse;
import com.example.BlindCafe.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.BlindCafe.config.jwt.JwtAuthorizationFilter.UID;

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
    public ResponseEntity<Void> createMatching(@RequestHeader(value = UID) String uid) {
        log.info("POST /api/matching");
        matchingService.createMatching(Long.parseLong(uid));
        return ResponseEntity.ok().build();
    }

    /**
     * 매칭 취소하기
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelMatching(@RequestHeader(value = UID) String uid) {
        log.info("POST /api/matching/cancel");
        matchingService.cancelMatching(Long.parseLong(uid));
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 리스트 조회
     */
    @GetMapping
    public ResponseEntity<MatchingListResponse> getMatchings(@RequestHeader(value = UID) String uid) {
        log.info("GET /api/matching");
        return ResponseEntity.ok(matchingService.getMatchings(Long.parseLong(uid)));
    }

    /**
     * 채팅방 정보 조회
     */
    @GetMapping("/{matchingId}")
    public ResponseEntity<MatchingDetailResponse> getMatching(
            @RequestHeader(value = UID) String uid,
            @PathVariable Long matchingId
    )  {
        log.info("GET /api/matching/{}", matchingId);
        return ResponseEntity.ok(matchingService.getMatching(Long.parseLong(uid), matchingId));
    }

    /**
     * 음료수 선택하기
     */
    @PostMapping("/drink")
    public ResponseEntity<Void> selectDrink(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody SelectDrinkRequest request
    ) {
        log.info("POST /api/matching/drink");
        matchingService.selectDrink(Long.parseLong(uid), request);
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
     * 프로필 공개 수락/거절하기
     */
    @PostMapping("/profile")
    public ResponseEntity<Void> openProfile(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody OpenProfileRequest request
    ) {
        log.info("POST /api/matching/profile");
        matchingService.openProfile(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 교환 수락/거절하기
     */
    @PostMapping("/exchange")
    public ResponseEntity<Void> exchangeProfile(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody ExchangeProfileRequest request
    ) {
        log.info("POST /api/matching/exchange");
        matchingService.exchangeProfile(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 나가기
     */
    @DeleteMapping("/{matchingId}")
    public ResponseEntity<Void> leaveMatching(
            @RequestHeader(value = UID) String uid,
            @PathVariable Long matchingId,
            @RequestParam(value = "reason", defaultValue = "1") Long reasonId
    ) {
        log.info("DELETE /api/matching/{}", matchingId);
        matchingService.leaveMatching(Long.parseLong(uid), matchingId, reasonId);
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 로그 찍기
     */
    @PostMapping("{matchingId}/log")
    public ResponseEntity<Void> createRoomLog(
            @RequestHeader(value = UID) String uid,
            @PathVariable Long matchingId
    ) {
        log.info("POST /api/matching/{}/log", matchingId);
        matchingService.createRoomLog(Long.parseLong(uid), matchingId);
        return ResponseEntity.ok().build();
    }
}
