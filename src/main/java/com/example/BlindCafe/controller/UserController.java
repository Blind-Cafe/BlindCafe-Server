package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.request.AddUserInfoRequest;
import com.example.BlindCafe.dto.request.UploadAvatarRequest;
import com.example.BlindCafe.dto.request.EditInterestRequest;
import com.example.BlindCafe.dto.request.EditProfileRequest;
import com.example.BlindCafe.dto.response.AvatarListResponse;
import com.example.BlindCafe.dto.response.DeleteUserResponse;
import com.example.BlindCafe.dto.response.UserDetailResponse;
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
            @Valid @RequestBody EditProfileRequest request
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
            @Valid @RequestBody EditInterestRequest request
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
            @RequestParam UploadAvatarRequest request
    ) {
        log.info("POST /api/user/avatar");
        userService.uploadAvatar(Long.parseLong(uid), request);
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
    public ResponseEntity<UserProfileResponse> getProfile(
            @PathVariable Long userId
    ) {
        log.info("GET /api/user/{}/profile", userId);
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
