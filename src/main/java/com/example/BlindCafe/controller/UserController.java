package com.example.BlindCafe.controller;

import com.example.BlindCafe.domain.Report;
import com.example.BlindCafe.dto.request.*;
import com.example.BlindCafe.dto.response.*;
import com.example.BlindCafe.service.MatchingService;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.example.BlindCafe.config.jwt.JwtAuthorizationFilter.UID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MatchingService matchingService;

    /**
     * 유저 정보 추가 입력(온보딩)
     */
    @PostMapping
    public ResponseEntity<Void> addUserInfo(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody AddUserInfoRequest request
    ) {
        log.info("POST /api/user");
        userService.addUserInfo(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 마이페이지 (유저 정보 조회)
     */
    @GetMapping
    public ResponseEntity<UserDetailResponse> getUser(@RequestHeader(value = UID) String uid) {
        log.info("GET /api/user");
        return ResponseEntity.ok(userService.getUser(Long.parseLong(uid)));
    }

    /**
     * 사용자 프로필 수정하기
     */
    @PutMapping
    public ResponseEntity<UserDetailResponse> editProfile(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("PUT /api/user");
        return ResponseEntity.ok(userService.editProfile(Long.parseLong(uid), request));
    }

    /**
     * 사용자 관심사 수정
     */
    @PutMapping("/interest")
    public ResponseEntity<Void> editInterest(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody UpdateInterestRequest request
    ) {
        log.info("PUT /api/user/interest");
        userService.editInterest(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 이미지 리스트 조회
     */
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<AvatarListResponse> getAvatars(@PathVariable Long userId) {
        log.info("GET /api/user/{}/avatar", userId);
        return ResponseEntity.ok(userService.getAvatars(userId));
    }

    /**
     * 프로필 이미지 업로드/수정
     */
    @PostMapping("/avatar")
    public ResponseEntity<Void> uploadAvatar(
            @RequestHeader(value = UID) String uid,
            @RequestParam UpdateAvatarRequest request
    ) {
        log.info("POST /api/user/avatar");
        userService.updateAvatar(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 이미지 삭제
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(
            @RequestHeader(value = UID) String uid,
            @RequestParam(name = "seq") int sequence
    ) {
        log.info("DELETE /api/user/avatar");
        userService.deleteAvatar(Long.parseLong(uid), sequence);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 목소리 설정하기
     */
    @PostMapping("/voice")
    public ResponseEntity<Void> updateVoice(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody UpdateVoiceRequest request
    ) {
        log.info("POST /api/user/voice");
        userService.updateVoice(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 목소리 삭제하기
     */
    @DeleteMapping("/voice")
    public ResponseEntity<Void> deleteVoice(@RequestHeader(value = UID) String uid) {
        log.info("DELETE /api/user/voice");
        userService.deleteVoice(Long.parseLong(uid));
        return ResponseEntity.ok().build();
    }

    /**
     * 유저 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<DeleteUserResponse> deleteUser(
            @RequestHeader(value = UID) String uid,
            @RequestParam(value="reason", defaultValue="1") Long reasonId
    ) {
        log.info("DELETE /api/user");
        return ResponseEntity.ok(userService.deleteUser(Long.parseLong(uid), reasonId));
    }

    /**
     * 채팅방 프로필(상대방) 조회
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        log.info("GET /api/user/{}/profile", userId);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    /**
     * 건의사항 작성하기
     */
    @PostMapping("/suggestion")
    public ResponseEntity<Void> suggest(@RequestHeader(value = UID) String uid, SuggestionRequest request) {
        log.info("POST /api/user/suggestion");
        userService.suggest(Long.parseLong(uid), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 신고하기
     */
    @PostMapping("/report")
    public ResponseEntity<Void> report(
            @RequestHeader(value = UID) String uid,
            @Valid @RequestBody ReportRequest request
    ) {
        log.info("POST /api/user/report");
        // 신고하기
        userService.report(Long.parseLong(uid), request);
        // 방 나가기 처리 - 나가기 사유는 5번으로 고정
        matchingService.leaveMatching(Long.parseLong(uid), request.getMatchingId(), 5L);
        return ResponseEntity.ok().build();
    }

    /**
     * 신고 내역 조회하기
     */
    @GetMapping("/report")
    public ResponseEntity<ReportListResponse> getReports(@RequestHeader(value = UID) String uid) {
        log.info("GET /api/user/report");
        return ResponseEntity.ok(userService.getReports(Long.parseLong(uid)));
    }
}
