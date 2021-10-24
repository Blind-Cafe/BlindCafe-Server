package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.LoginDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.type.AgeRange;
import com.example.BlindCafe.type.Gender;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.Map;

import static com.example.BlindCafe.auth.variable.KAKAO_AUTH;
import static com.example.BlindCafe.exception.ErrorCode.*;
import static com.example.BlindCafe.type.AgeRange.getAgeRange;
import static com.example.BlindCafe.type.Gender.*;
import static com.example.BlindCafe.type.Social.KAKAO;
import static com.example.BlindCafe.type.status.UserStatus.NORMAL;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void signinByKakao(LoginDto.Request request) {
        LoginDto.KaKaoResponse kaKaoResponse = getInfoByKakaoToken(request.getToken());

        User user = User.builder()
                .socialId(kaKaoResponse.getSocialId())
                .socialType(kaKaoResponse.getSocialType())
                .ageRange(kaKaoResponse.getAgeRange())
                .myGender(kaKaoResponse.getMyGender())
                .status(NORMAL)
                .build();
        userRepository.save(user);

        System.out.println(user);
    }

    /**
     * 카카오 엑세스 토큰으로 유저 정보 얻기
     * @param token
     * @return 카카오 유저 정보
     */
    private LoginDto.KaKaoResponse getInfoByKakaoToken(String token) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = URI.create(KAKAO_AUTH);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
            HttpEntity<MultiValueMap<String, Object>> restRequest = new HttpEntity<>(parameters, headers);
            ResponseEntity<JSONObject> apiResponse = restTemplate.postForEntity(uri, restRequest, JSONObject.class);
            JSONObject responseBody = apiResponse.getBody();

            String socialId = String.valueOf(responseBody.get("id"));
            JSONObject kakaoAccount = new JSONObject((Map) responseBody.get("kakao_account"));
            AgeRange ageRange = getAgeRange((String) kakaoAccount.get("age_range"));
            Gender gender = getGender((String) kakaoAccount.get("gender"));

            return LoginDto.KaKaoResponse.builder()
                    .socialId(socialId)
                    .socialType(KAKAO)
                    .ageRange(ageRange)
                    .myGender(gender)
                    .build();
        } catch (HttpClientErrorException e) {
            throw new BlindCafeException(INVALID_KAKAO_TOKEN);
        } catch (HttpServerErrorException e) {
            throw new BlindCafeException(INVALID_KAKAO_ACCESS);
        } catch (Exception e) {
            throw new BlindCafeException(UNAUTHORIZED_KAKAO_TOKEN);
        }
    }
}
