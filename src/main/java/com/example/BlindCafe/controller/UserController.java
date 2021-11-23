package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.CreateUserInfoDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.example.BlindCafe.config.SecurityConfig.getUserId;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /**
     * 마이페이지 (유저 정보 조회)
     */
    @GetMapping
    public ResponseEntity<UserDetailDto> getUserDetail(Authentication authentication) {
        log.info("GET /api/user");
        return ResponseEntity.ok(userService.getUserDetail(getUserId(authentication)));
    }

    /**
     * 유저 정보 추가 입력(온보딩)
     */
    @PostMapping
    public ResponseEntity<CreateUserInfoDto.Response> addUserInfo(
            Authentication authentication,
            @Valid @RequestBody CreateUserInfoDto.Request request
    ) {
        log.info("POST /api/user");
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.addUserInfo(user, request));
    }

    /**
     * 홈화면 (유저 매칭 상태 확인)
     */
    @GetMapping("/home")
    public ResponseEntity<UserHomeDto.Response> userHome(Authentication authentication) {
        log.info("GET /api/user/home");
        return ResponseEntity.ok(userService.userHome(getUserId(authentication)));
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
    public ResponseEntity<EditUserDto.Response> getMyProfileForEdit(
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
