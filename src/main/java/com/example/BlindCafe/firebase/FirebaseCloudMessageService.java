package com.example.BlindCafe.firebase;

import com.example.BlindCafe.dto.FcmMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseCloudMessageService {
    private final String KEY_PATH = "firebase/firebase-service-key.json";
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/fcm-test-6c962/messages:send";
    private final String AUTH_URL = "https://www.googleapis.com/auth/cloud-platform";

    private final ObjectMapper objectMapper;

    public void sendMessageTo(String targetToken, String title, String body, String path) {
        try {
            String message = makeMessage(targetToken, title, body, path);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();

            Response response  = client.newCall(request).execute();

            log.info(response.body().string());
        } catch (Exception e) {
            log.error(e.getClass() + " : " + e.getMessage());
        }

    }

    private String makeMessage(String targetToken, String title, String body, String path) throws JsonProcessingException {
        FcmMessageDto fcmMessage = FcmMessageDto.builder()
                .message(FcmMessageDto.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessageDto.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        )
                        .path(path)
                        .build()
                ).validate_only(false).build();

        log.info(objectMapper.writeValueAsString(fcmMessage));
        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws Exception {
        String firebaseConfigPath = KEY_PATH;

        GoogleCredentials googleCredentials =
                GoogleCredentials.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                        .createScoped(List.of(AUTH_URL));

        // accessToken 생성
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
