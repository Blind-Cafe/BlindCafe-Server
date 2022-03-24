package com.example.BlindCafe.controller;

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
     * 채팅방 로그 찍기
     */
    @PostMapping("{matchingId}/log")
    public ResponseEntity<Void> createRoomLog(
            Authentication authentication,
            @PathVariable Long matchingId
    ) {
        log.info("POST /api/matching/{}/log", matchingId);
        matchingService.createRoomLog(getUserId(authentication), matchingId);
        return ResponseEntity.ok().build();
    }
}
