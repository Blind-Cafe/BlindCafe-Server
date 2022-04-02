package com.example.BlindCafe.service;

import com.example.BlindCafe.config.jwt.JwtProperties;
import com.example.BlindCafe.config.jwt.JwtUtils;
import com.example.BlindCafe.domain.MatchingHistory;
import com.example.BlindCafe.domain.NotificationSetting;
import com.example.BlindCafe.domain.Ticket;
import com.example.BlindCafe.dto.request.LoginRequest;
import com.example.BlindCafe.dto.request.RefreshTokenRequest;
import com.example.BlindCafe.dto.response.LoginResponse;
import com.example.BlindCafe.dto.response.RefreshTokenResponse;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.type.status.UserStatus;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.domain.type.Social;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.util.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.example.BlindCafe.config.jwt.JwtUtils.*;
import static com.example.BlindCafe.domain.type.status.UserStatus.*;
import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.domain.type.Social.APPLE;
import static com.example.BlindCafe.domain.type.Social.KAKAO;
import static com.example.BlindCafe.service.NotificationService.deviceInfoInMemory;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate redisTemplate;
    private final UserRepository userRepository;

    private final String KAKAO_AUTH = "https://kapi.kakao.com/v2/user/me";
    private final String APPLE_AUTH = "https://appleid.apple.com/auth/keys";

    /**
     * 로그인
     */
    @Transactional
    public Pair<HttpStatus, LoginResponse> login(LoginRequest request) {

        String socialId = getSocialId(request.getSocial(), request.getAccessToken());
        String deviceToken = request.getDeviceToken();
        Platform platform = request.getPlatform();

        Optional<User> userOptional = userRepository.findBySocialId(socialId);
        boolean isRegistered = userOptional.isPresent();

        // 회원가입
        if (!isRegistered) {
            // 티켓(매칭권) 생성
            Ticket ticket = Ticket.create();
            // 매칭 히스토리 생성
            MatchingHistory history = MatchingHistory.create();
            // 알림 설정
            NotificationSetting setting = NotificationSetting.create();

            User newUser = User.create(request.getSocial(), socialId, platform, deviceToken, ticket, history, setting);
            userRepository.save(newUser);
            Pair<String, String> tokens = getTokens(newUser);

            return Pair.of(
                    HttpStatus.CREATED,
                    new LoginResponse(newUser.getId(), tokens.getFirst(), tokens.getSecond())
            );
        }

        User user = userOptional.get();
        validateUser(user);

        // 디바이스 토큰 및 기종 업데이트
        user.updateDevice(platform, deviceToken);
        deviceInfoInMemory.put(user.getId(), Pair.of(platform, deviceToken));

        // access token & refresh token
        Pair<String, String> tokens = getTokens(user);

        // 필수 정보가 입력되지 않은 경우 핸들링
        if (user.getStatus().equals(NOT_YET)) {
            return Pair.of(
                    HttpStatus.CREATED,
                    new LoginResponse(user.getId(), tokens.getFirst(), tokens.getSecond())
            );
        }

        return Pair.of(
                HttpStatus.OK,
                new LoginResponse(user.getId(), user.getNickname(), tokens.getFirst(), tokens.getSecond())
        );
    }

    // 엑세스 토큰으로 소셜계정 고유 ID 얻기
    private String getSocialId(Social social, String accessToken) {
        if (social.equals(KAKAO))
            return verifyKakaoToken(accessToken);
        else if (social.equals(APPLE))
            return verifyAppleToken(accessToken);
        else
            throw new BlindCafeException(INVALID_SOCIAL_PLATFORM);
    }

    // 카카오 엑세스 토큰 유효성 검증
    private String verifyKakaoToken(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = URI.create(KAKAO_AUTH);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
            HttpEntity<MultiValueMap<String, Object>> restRequest = new HttpEntity<>(parameters, headers);
            ResponseEntity<JSONObject> apiResponse = restTemplate.postForEntity(uri, restRequest, JSONObject.class);
            JSONObject responseBody = apiResponse.getBody();

            return String.valueOf(responseBody.get("id"));
        } catch (HttpClientErrorException e) {
            throw new BlindCafeException(INVALID_KAKAO_TOKEN);
        } catch (HttpServerErrorException e) {
            throw new BlindCafeException(INVALID_KAKAO_ACCESS);
        }
    }

    // 애플 엑세스 토큰 유효성 검증
    private String verifyAppleToken(String token) {
        String[] decodeArray = token.split("\\.");
        String headerStr = new String(Base64.getDecoder().decode(decodeArray[0]));
        String payloadStr = new String(Base64.getDecoder().decode(decodeArray[1]));
        // 공개키
        JSONObject publicKey = getApplePublicKey(headerStr);
        try {
            JSONParser parser = new JSONParser();
            JSONObject payload = (JSONObject) parser.parse(payloadStr);
            return String.valueOf(payload.get("sub"));
        } catch (ParseException e) {
            throw new BlindCafeException(INVALID_APPLE_TOKEN);
        }
    }

    // 애플 공개키 조회
    private JSONObject getApplePublicKey(String headerStr) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = URI.create(APPLE_AUTH);
            ResponseEntity<JSONObject> apiResponse = restTemplate.getForEntity(uri, JSONObject.class);
            JSONObject responseBody = apiResponse.getBody();
            ArrayList<JSONObject> keyArray = (ArrayList<JSONObject>) responseBody.get("keys");

            JSONParser parser = new JSONParser();
            JSONObject header = (JSONObject) parser.parse(headerStr);
            JSONObject availableKey = null;

            for (int i=0; i<keyArray.size(); i++) {
                JSONObject key = new JSONObject(keyArray.get(i));
                if (key.get("kid").equals(header.get("kid")) &&
                        key.get("alg").equals(header.get("alg"))) {
                    availableKey = key;
                }
            }
            if (ObjectUtils.isEmpty(availableKey))
                throw new BlindCafeException(FAILED_TO_FIND_AVAILABLE_RSA);
            return availableKey;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new BlindCafeException(INVALID_APPLE_ACCESS);
        } catch (ParseException e) {
            throw new BlindCafeException(INVALID_APPLE_TOKEN);
        }
    }

    // access token, refresh token 생성
    private Pair<String, String> getTokens(User user) {
        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        String uid = user.getId().toString();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(
                JwtProperties.REFRESH_TOKEN_PREFIX + uid,
                refreshToken,
                JwtProperties.REFRESH_EXPIRED_TIME,
                TimeUnit.MILLISECONDS
        );
        return Pair.of(accessToken, refreshToken);
    }

    /**
     * 토큰 갱신
     */
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String uid;
        try {
            uid = JwtUtils.getUsedId("Bearer " + request.getRefreshToken());
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String refreshToken = valueOperations.get(JwtProperties.REFRESH_TOKEN_PREFIX + uid);
            if (!request.getRefreshToken().equals(refreshToken))
                throw new BlindCafeException(FAILED_AUTHORIZATION);
        } catch (ExpiredJwtException e) {
            throw new BlindCafeException(EXPIRED_TOKEN);
        } catch (IllegalArgumentException | MalformedJwtException e) {
            throw new BlindCafeException(FAILED_AUTHORIZATION);
        }

        User user = userRepository.findById(Long.parseLong(uid))
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        validateUser(user);

        return new RefreshTokenResponse(createAccessToken(user));
    }

    // 사용자 상태 유효성 검사
    private void validateUser(User user) {
        if (user.getStatus().equals(SUSPENDED))
            throw new BlindCafeException(SUSPENDED_USER, user.getNickname());
        if (user.getStatus().equals(UserStatus.RETIRED))
            throw new BlindCafeException(RETIRED_USER);
    }
}
