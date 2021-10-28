package com.example.BlindCafe.auth;

import com.example.BlindCafe.exception.BlindCafeException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

public class SocialUtils {

    private static final String KAKAO_AUTH = "https://kapi.kakao.com/v2/user/me";
    private static final String APPLE_AUTH = "https://appleid.apple.com/auth/keys";

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
        }
    }

    public static String verifyAppleToken(String token) {
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

    private static JSONObject getApplePublicKey(String headerStr) {
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
}
