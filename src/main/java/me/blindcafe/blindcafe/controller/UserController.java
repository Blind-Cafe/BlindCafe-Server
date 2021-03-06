package me.blindcafe.blindcafe.controller;

import me.blindcafe.blindcafe.dto.request.*;
import me.blindcafe.blindcafe.dto.response.*;
import me.blindcafe.blindcafe.service.MatchingService;
import me.blindcafe.blindcafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.util.List;

import static me.blindcafe.blindcafe.config.SecurityConfig.getUid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MatchingService matchingService;

    /**
     * 전화번호 중복 확인
     */
    @PostMapping("/phone-check")
    public ResponseEntity<PhoneCheckResponse> isDuplicatedPhoneNumber(@Valid @RequestBody PhoneCheckRequest request) {
        log.info("POST /api/auth/phone-check");
        boolean status = userService.isDuplicatedPhoneNumber(request.getPhone());
        return ResponseEntity.ok(new PhoneCheckResponse(status));
    }

    /**
     * 유저 정보 추가 입력(온보딩)
     */
    @PostMapping
    public ResponseEntity<Void> addUserInfo(
            Authentication authentication,
            @Valid @RequestBody AddUserInfoRequest request
    ) {
        log.info("POST /api/user");
        userService.addUserInfo(getUid(authentication), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 마이페이지 (유저 정보 조회)
     */
    @GetMapping
    public ResponseEntity<UserDetailResponse> getUser(Authentication authentication) {
        log.info("GET /api/user");
        return ResponseEntity.ok(userService.getUser(getUid(authentication)));
    }

    /**
     * 사용자 프로필 수정하기
     */
    @PutMapping
    public ResponseEntity<UserDetailResponse> editProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("PUT /api/user");
        return ResponseEntity.ok(userService.editProfile(getUid(authentication), request));
    }

    /**
     * 사용자 관심사 수정
     */
    @PutMapping("/interest")
    public ResponseEntity<Void> editInterest(
            Authentication authentication,
            @Valid @RequestBody UpdateInterestRequest request
    ) {
        log.info("PUT /api/user/interest");
        userService.editInterest(getUid(authentication), request);
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
            Authentication authentication,
            @RequestParam int sequence,
            @RequestParam MultipartFile image
    ) {
        log.info("POST /api/user/avatar");
        userService.updateAvatar(getUid(authentication), sequence, image);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 이미지 삭제
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(
            Authentication authentication,
            @RequestParam(name = "seq") int sequence
    ) {
        log.info("DELETE /api/user/avatar");
        userService.deleteAvatar(getUid(authentication), sequence);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 목소리 설정하기
     */
    @PostMapping("/voice")
    public ResponseEntity<Void> updateVoice(
            Authentication authentication,
            @RequestParam MultipartFile voice
    ) {
        log.info("POST /api/user/voice");
        userService.updateVoice(getUid(authentication), voice);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 목소리 삭제하기
     */
    @DeleteMapping("/voice")
    public ResponseEntity<Void> deleteVoice(Authentication authentication) {
        log.info("DELETE /api/user/voice");
        userService.deleteVoice(getUid(authentication));
        return ResponseEntity.ok().build();
    }

    /**
     * 유저 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<DeleteUserResponse> deleteUser(
            Authentication authentication,
            @RequestParam(value="reason", defaultValue="1") Long reasonId
    ) {
        log.info("DELETE /api/user");
        return ResponseEntity.ok(userService.deleteUser(getUid(authentication), reasonId));
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
    public ResponseEntity<Void> suggest(
            Authentication authentication,
            @RequestParam String content,
            @RequestParam(value = "images", required=false) List<MultipartFile> images
    ) {
        log.info("POST /api/user/suggestion");
        userService.suggest(getUid(authentication), content, images);
        return ResponseEntity.ok().build();
    }

    /**
     * 신고하기
     */
    @PostMapping("/report")
    public ResponseEntity<Void> report(
            Authentication authentication,
            @Valid @RequestBody ReportRequest request
    ) {
        log.info("POST /api/user/report");
        // 신고하기
        userService.report(getUid(authentication), request);
        // 방 나가기 처리 - 나가기 사유는 4번으로 고정
        matchingService.leaveMatching(getUid(authentication), request.getMatchingId(), 4L);
        return ResponseEntity.ok().build();
    }

    /**
     * 신고 내역 조회하기
     */
    @GetMapping("/report")
    public ResponseEntity<ReportListResponse> getReports(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        log.info("GET /api/user/report");
        return ResponseEntity.ok(userService.getReports(getUid(authentication), page, size));
    }
}
