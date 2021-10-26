package com.example.BlindCafe.auth;

import com.example.BlindCafe.exception.BlindCafeException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

public class SocialUtils {

    private static final String KAKAO_AUTH = "https://kapi.kakao.com/v2/user/me";
    private static final String APPLE_AUTH = "https://appleid.apple.com/auth/token";

    public static String verifyKakaoToken(String token) {
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
        } catch (Exception e) {
            throw new BlindCafeException(INTERNAL_SERVER_ERROR);
        }
    }



}
