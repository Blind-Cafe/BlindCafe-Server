package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.LoginDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.type.DeviceType;
import com.example.BlindCafe.type.Social;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static com.example.BlindCafe.auth.SocialUtils.verifyAppleToken;
import static com.example.BlindCafe.auth.SocialUtils.verifyKakaoToken;
import static com.example.BlindCafe.auth.jwt.JwtUtils.createToken;
import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.type.Social.APPLE;
import static com.example.BlindCafe.type.Social.KAKAO;
import static com.example.BlindCafe.type.status.UserStatus.NOT_REQUIRED_INFO;
import static com.example.BlindCafe.type.status.UserStatus.SUSPENDED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public LoginDto.Response signin(LoginDto.Request request, Social social) {
        LoginDto.SocialResponse socialResponse = getInfoByToken(request.getToken(), social);
        String deviceId = request.getDeviceId();
        DeviceType deviceType = request.getDeviceType();
        if (Objects.isNull(deviceType))
            deviceType = DeviceType.AOS;

        Optional<User> userOptional = userRepository.findBySocialId(socialResponse.getSocialId());
        boolean isRegistered = userOptional.isPresent();

        if (isRegistered) {
            User user = userOptional.get();
            // 신고 유저
            if (user.getStatus().equals(SUSPENDED))
                throw new BlindCafeException(SUSPENDED_USER, user.getNickname());
            else {
                // 디바이스 토큰 업데이트
                if (!user.getDeviceId().equals(deviceId)) {
                    user.setDeviceId(deviceId);
                }
                // 기종 업데이트
                if (!user.getDeviceType().equals(deviceType)) {
                    user.setDeviceType(deviceType);
                }
                if (user.getStatus().equals(NOT_REQUIRED_INFO)) {
                    // 필수 정보가 입력되지 않은 경우
                    return getLoginResponse(user, SIGN_IN_NOT_REQUIRED_INFO);
                } else {
                    // 필수 정보가 입력된 경우 정상 로그인
                    return getLoginResponse(user, SIGN_IN);
                }
            }
        } else {
            // 회원가입
            User user = User.builder()
                    .socialId(socialResponse.getSocialId())
                    .socialType(socialResponse.getSocialType())
                    .deviceId(deviceId)
                    .deviceType(deviceType)
                    .status(UserStatus.NOT_REQUIRED_INFO)
                    .build();
            userRepository.save(user);
            return getLoginResponse(user, SIGN_UP);
        }
    }

    /**
     * 엑세스 토큰으로 유저 정보 얻기
     * @param token
     * @return 유저 정보
     */
    private LoginDto.SocialResponse getInfoByToken(String token, Social social) {
        if (social.equals(KAKAO)) {
            // 카카오 로그인
            return LoginDto.SocialResponse.builder()
                    .socialId(verifyKakaoToken(token))
                    .socialType(KAKAO)
                    .build();
        } else if (social.equals(APPLE)) {
            return LoginDto.SocialResponse.builder()
                    .socialId(verifyAppleToken(token))
                    .socialType(APPLE)
                    .build();
        } else {
            throw new BlindCafeException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 토큰 생성 및 반환하기
     */
    private LoginDto.Response getLoginResponse(User user, CodeAndMessage codeAndMessage) {
        String token = createToken(user);
        String nickname = user.getNickname() != null ? user.getNickname() : null;
        return LoginDto.Response.builder()
                .codeAndMessage(codeAndMessage)
                .jwt(token)
                .id(user.getId())
                .nickname(nickname)
                .build();
    }
}
