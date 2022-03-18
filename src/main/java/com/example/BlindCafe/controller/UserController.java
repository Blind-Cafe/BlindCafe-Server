package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.request.AddUserInfoRequest;
import com.example.BlindCafe.dto.request.EditProfileRequest;
import com.example.BlindCafe.dto.response.UserDetailResponse;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * 프로필 이미지 리스트 조회
     */
    @GetMapping("{userId}/image")
    public ResponseEntity<ProfileImageListDto> getProfileImages(
            Authentication authentication,
            @PathVariable Long userId
    ) {
        log.info("GET /api/user/{}/image", userId);
        return ResponseEntity.ok(userService.getProfileImages(userId));
    }

    /**
     * 밝은 채팅방 프로필 조회
     */
    @GetMapping("{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            Authentication authentication,
            @PathVariable Long userId
    ) {
        log.info("GET /api/user/{}/profile", userId);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    /**
     * 유저 관심사 수정
     */
    @PutMapping("/interest")
    public ResponseEntity<EditInterestDto.Response> editInterest(
            Authentication authentication,
            @Valid @RequestBody EditInterestDto.Request request
    ) {
        log.info("PUT /api/user/interest");
        return ResponseEntity.ok(userService.editInterest(getUserId(authentication), request));
    }

    /**
     * 프로필 사진 수정 화면
     */
    @GetMapping("/image")
    public ResponseEntity<ProfileImageListDto> getProfileImagesForEdit(
            Authentication authentication
    ) {
        log.info("GET /api/user/image");
        return ResponseEntity.ok(userService.getProfileImagesForEdit(getUserId(authentication)));
    }
    /**
     * 유저 프로필 이미지 수정
     */
    @PatchMapping("/image")
    public ResponseEntity<Void> editProfileImage(
            Authentication authentication,
            @RequestParam int priority,
            @RequestParam MultipartFile image
    ) {
        log.info("PATCH /api/user/image");
        userService.editProfileImage(getUserId(authentication), priority, image);
        return ResponseEntity.ok().build();
    }

    /**
     * 유저 프로필 이미지 삭제
     */
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteProfileImage(
            Authentication authentication,
            @RequestParam int priority
    ) {
        log.info("DELETE /api/user/image");
        userService.deleteProfileImage(getUserId(authentication), priority);
        return ResponseEntity.ok().build();
    }

    /**
     * 유저 닉네임 수정
     */
    @PatchMapping("/nickname")
    public ResponseEntity<EditNicknameDto.Response> editNickname (
            Authentication authentication,
            @Valid @RequestBody EditNicknameDto.Request request
    ) {
        log.info("PATCH /api/user/nickname");
        return ResponseEntity.ok(userService.editNickname(getUserId(authentication), request));
    }

    /**
     * 유저 주소 수정
     */
    @PatchMapping("/address")
    public ResponseEntity<EditAddressDto.Response> editAddress(
            Authentication authentication,
            @Valid @RequestBody EditAddressDto.Request request
    ) {
        log.info("PATCH /api/user/address");
        return ResponseEntity.ok(userService.editAddress(getUserId(authentication), request));
    }

    /**
     * 디바이스 토큰 갱신
     */
    @PatchMapping("device")
    public ResponseEntity<Void> updatedDeviceToken(
            Authentication authentication,
            @Valid @RequestBody EditDeviceToken request
    ) {
        log.info("PATCH /api/user/device");
        userService.updateDeviceToken(getUserId(authentication), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 매칭 상대방 성별 수정
     */
    @PatchMapping("/partner")
    public ResponseEntity<Void> editPartnerGender(
            Authentication authentication,
            @Valid @RequestBody EditPartnerGenderDto request
    ) {
        log.info("PATCH /api/user/partner");
        userService.editPartnerGender(getUserId(authentication), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 수정 조회 화면
     */
    @GetMapping("profile")
    public ResponseEntity<EditUserProfileDto.Response> getMyProfileForEdit(
            Authentication authentication
    ) {
        log.info("GET /api/user/profile");
        return ResponseEntity.ok(userService.getMyProfileForEdit(getUserId(authentication)));
    }

    /**
     * 유저 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<DeleteUserDto.Response> deleteUser(
            Authentication authentication,
            @RequestParam(value="reason", defaultValue="1") Long reasonNum
    ) {
        log.info("DELETE /api/user");
        return ResponseEntity.ok(userService.deleteUser(getUserId(authentication), reasonNum));
    }
}
