package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public ResponseEntity<MatchingListDto> getMatchings(Authentication authentication) {
        log.info("GET /api/matching");
        return ResponseEntity.ok(matchingService.getMatchings(getUserId(authentication)));
    }

    /**
     * 채팅방 정보 조회
     */
    @GetMapping("{matchingId}")
    public ResponseEntity<MatchingDetailDto> getMatching(
            Authentication authentication,
            @PathVariable Long matchingId
    )  {
        log.info("GET /api/matching/{}", matchingId);
        return ResponseEntity.ok(matchingService.getMatching(getUserId(authentication), matchingId));
    }

    /**
     * 매칭 요청하기
     */
    @PostMapping
    public ResponseEntity<CreateMatchingDto.Response> createMatching(Authentication authentication) {
        log.info("POST /api/matching");
        return ResponseEntity.ok(matchingService.createMatching(getUserId(authentication)));
    }

    /**
     * 채팅방 나가기
     */
    @DeleteMapping("{matchingId}")
    public ResponseEntity<DeleteMatchingDto> deleteMatching(
            Authentication authentication,
            @PathVariable Long matchingId,
            @RequestParam(value = "reason") Long reasonNum
    ) {
        log.info("DELETE /api/matching/{}", matchingId);
        return ResponseEntity.ok(matchingService.deleteMatching(getUserId(authentication), matchingId, reasonNum));
    }

    /**
     * 음료수 설정하기
     */
    @PostMapping("{matchingId}/drink")
    public ResponseEntity<DrinkDto.Response> setDrink(
            Authentication authentication,
            @PathVariable Long matchingId,
            @Valid @RequestBody DrinkDto.Request request
    ) {
        log.info("POST /api/matching/{}/drink", matchingId);
        return ResponseEntity.ok(matchingService.setDrink(getUserId(authentication), matchingId, request));
    }

    /**
     * 프로필 교환 시 내 프로필 조회
     */
    @GetMapping("{matchingId}/profile")
    public ResponseEntity<MatchingProfileDto> getMatchingProfile(
            Authentication authentication,
            @PathVariable Long matchingId
    ) {
        log.info("GET /api/matching/{}/profile", matchingId);
        return ResponseEntity.ok(matchingService.getMatchingProfile(getUserId(authentication), matchingId));
    }

    /**
     * 프로필 공개하기
     */
    @PostMapping("{matchingId}/profile")
    public ResponseEntity<OpenMatchingProfileDto.Response> openMatchingProfile(
            Authentication authentication,
            @PathVariable Long matchingId,
            @Valid @RequestBody OpenMatchingProfileDto.Request request
    ) {
        log.info("POST /api/matching/{}/profile", matchingId);
        return ResponseEntity.ok(
                matchingService.openMatchingProfile(getUserId(authentication), matchingId, request));
    }

    /**
     * 상대방 프로필 확인하기
     */
    @GetMapping("{matchingId}/partner")
    public ResponseEntity<MatchingProfileDto> getPartnerProfile(
            Authentication authentication,
            @PathVariable Long matchingId
    ) {
        log.info("GET /api/matching/{}/partner", matchingId);
        return ResponseEntity.ok(
                matchingService.getPartnerProfile(getUserId(authentication), matchingId));
    }

    /**
     * 프로필 교환 수락하기
     */
    @PostMapping("{matchingId}/partner")
    public ResponseEntity<OpenMatchingProfileDto.Response> acceptExchangeProfile(
            Authentication authentication,
            @PathVariable Long matchingId
    ) {
        log.info("POST /api/matching/{}/partner", matchingId);
        return ResponseEntity.ok(matchingService.acceptPartnerProfile(getUserId(authentication), matchingId));
    }

    /**
     * 프로필 교환 거절하기
     */
    @DeleteMapping("{matchingId}/partner")
    public ResponseEntity<Void> rejectExchangeProfile(
            Authentication authentication,
            @PathVariable Long matchingId,
            @RequestParam(value = "reason") Long reasonNum
    ) {
        log.info("DELETE /api/matching/{}/partner", matchingId);
        matchingService.rejectExchangeProfile(getUserId(authentication), matchingId, reasonNum);
        return ResponseEntity.ok().build();
    }

    /**
     * 토픽 가져오기
     */
    @GetMapping("{matchingId}/topic")
    public ResponseEntity<TopicDto> getTopic(
            Authentication authentication,
            @PathVariable Long matchingId
    ) {
        log.info("GET /api/matching/{}/topic", matchingId);
        return ResponseEntity.ok(matchingService.getTopic(getUserId(authentication), matchingId));
    }

    /**
     * 매칭 취소하기
     * Todo
     * matching이랑 room 분리하기 uri에 행위(cancel) 들어가는 건 별로
     */
    @PostMapping("cancel")
    public ResponseEntity<Void> cancelMatching(Authentication authentication) {
        log.info("POST /api/matching/cancel");
        matchingService.cancelMatching(getUserId(authentication));
        return ResponseEntity.ok().build();
    }
}
