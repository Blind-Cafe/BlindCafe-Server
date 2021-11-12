package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.CreateUserInfoDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public UserDetailDto getUserDetail(Authentication authentication) {
        log.info("GET /api/user");
        return userService.getUserDetail(getUserId(authentication));
    }

    /**
     * 유저 정보 추가 입력(온보딩)
     */
    @PostMapping
    public CreateUserInfoDto.Response addUserInfo(
            Authentication authentication,
            @Valid @RequestBody CreateUserInfoDto.Request request
    ) {
        log.info("POST /api/user");
        User user = (User) authentication.getPrincipal();
        return userService.addUserInfo(user, request);
    }

    /**
     * 홈화면 (유저 매칭 상태 확인)
     */
    @GetMapping("/home")
    public UserHomeDto.Response userHome(Authentication authentication) {
        log.info("GET /api/user/home");
        return userService.userHome(getUserId(authentication));
    }

    /**
     * 유저 관심사 수정
     */
    @PutMapping("/interest")
    public EditInterestDto.Response editInterest(
            Authentication authentication,
            @Valid @RequestBody EditInterestDto.Request request
    ) {
        log.info("PUT /api/user/interest");
        User user = (User) authentication.getPrincipal();
        return userService.editInterest(user, request);
    }

    /**
     * 유저 닉네임 수정
     */
    @PatchMapping("/nickname")
    public EditNicknameDto.Response editNickname (
            Authentication authentication,
            @Valid @RequestBody EditNicknameDto.Request request
    ) {
        log.info("PATCH /api/user/nickname");
        return userService.editNickname(getUserId(authentication), request);
    }

    /**
     * 유저 주소 수정
     */
    @PatchMapping("/address")
    public EditAddressDto.Response editAddress(
            Authentication authentication,
            @Valid @RequestBody EditAddressDto.Request request
    ) {
        log.info("PATCH /api/user/address");
        return userService.editAddress(getUserId(authentication), request);
    }

    /**
     * 유저 프로필 이미지 수정
     */
    @PatchMapping("/image")
    public EditProfileImageDto.Response editProfileImage(
            Authentication authentication,
            @Valid @RequestBody EditProfileImageDto.Request request
    ) {
        log.info("PATCH /api/user/image");
        return userService.editProfileImage(getUserId(authentication), request);
    }

    /**
     * 유저 탈퇴
     */
    @DeleteMapping
    public DeleteUserDto.Response deleteUser(
            Authentication authentication,
            @RequestParam(value="reason", defaultValue="0") Long reasonNum
    ) {
        log.info("DELETE /api/user");
        return userService.deleteUser(getUserId(authentication), reasonNum);
    }
}
