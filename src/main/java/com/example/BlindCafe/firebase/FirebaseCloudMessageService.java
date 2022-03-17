package com.example.BlindCafe.firebase;

import com.example.BlindCafe.dto.FcmMessageDto;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.entity.type.Platform;
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

import static com.example.BlindCafe.exception.CodeAndMessage.FCM_JSON_PARSE_ERROR;
import static com.example.BlindCafe.exception.CodeAndMessage.FCM_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseCloudMessageService {
    private static final String KEY_PATH = "firebase/blind-cafe-firebase-key.json";
    private static final String API_URL = "https://fcm.googleapis.com/v1/projects/blind-cafe/messages:send";
    private static final String AUTH_URL = "https://www.googleapis.com/auth/cloud-platform";

    private final ObjectMapper objectMapper;

    public void sendMessageTo(String targetToken, String title, String body, String path, String type, Long matchingId, Platform platform) {
        try {
            String message = makeMessage(targetToken, title, body, path, type, matchingId, platform);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();

            Response response  = client.newCall(request).execute();
        } catch (Exception e) {
            throw new BlindCafeException(FCM_SERVER_ERROR);
        }
    }

    private String makeMessage(String targetToken, String title, String body, String path, String type, Long matchingId, Platform platform) {
        try {
            /**
             * deviceType에 따라서 message 만들기
             */
            
            FcmMessageDto fcmMessage = FcmMessageDto.builder()
                    .message(FcmMessageDto.Message.builder()
                            .token(targetToken)
                            .notification(FcmMessageDto.Notification.builder()
                                    .title(title)
                                    .body(body)
                                    .image(null)
                                    .build()
                            )
                            .data(FcmMessageDto.FcmData.builder()
                                    .title(title)
                                    .body(body)
                                    .path(path)
                                    .type(type)
                                    .matchingId(Long.toString(matchingId))
                                    .build()
                            ).apns(FcmMessageDto.Apns.builder()
                                    .payload(FcmMessageDto.Payload.builder()
                                            .aps(FcmMessageDto.Aps.builder()
                                                    .title(title)
                                                    .body(body)
                                                    .path(path)
                                                    .type(type)
                                                    .matchingId(Long.toString(matchingId))
                                                    .build()).build()
                                    ).build()
                            )
                            .build()
                    ).validate_only(false).build();

            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            throw new BlindCafeException(FCM_JSON_PARSE_ERROR);
        }
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
