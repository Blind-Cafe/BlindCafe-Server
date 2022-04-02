package com.example.BlindCafe.restdocs;

import com.example.BlindCafe.domain.type.Platform;
import com.example.BlindCafe.domain.type.Social;
import com.example.BlindCafe.dto.request.LoginRequest;
import com.example.BlindCafe.dto.request.RefreshTokenRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthApiDocumentTest extends ApiDocumentTest {

    @DisplayName("회원가입")
    @Test
    public void 회원가입() throws Exception {
        String body = objectMapper.writeValueAsString(
                new LoginRequest(
                        Platform.IOS,
                        Social.KAKAO,
                        "Gb8izPbvhEzJ_q7wuWTBywQYI0zGlOQlQmVepQo9dRoAAAF_6b6CgA",
                        "Sample FCM Device Token"
                ));

        mockMvc.perform(
                post("/api/auth/login")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("auth/join",
                        requestFields(
                                fieldWithPath("platform").description("장비 플랫폼"),
                                fieldWithPath("social").description("소셜 계정 플랫폼"),
                                fieldWithPath("accessToken").description("소셜 계정 플랫폼 엑세스 토큰"),
                                fieldWithPath("deviceToken").description("FCM 디바이스 토큰")
                        ),
                        responseFields(
                                fieldWithPath("uid").description("사용자 ID"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("accessToken").description("엑세스 토큰"),
                                fieldWithPath("refreshToken").description("리프레쉬 토큰")
                        )
                ));
    }

    @DisplayName("로그인")
    @Test
    public void 로그인() throws Exception {
        String body = objectMapper.writeValueAsString(
                new LoginRequest(
                        Platform.IOS,
                        Social.KAKAO,
                        "Gb8izPbvhEzJ_q7wuWTBywQYI0zGlOQlQmVepQo9dRoAAAF_6b6CgA",
                        "Sample FCM Device Token"
                ));

        mockMvc.perform(
                post("/api/auth/login")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("auth/login",
                        requestFields(
                                fieldWithPath("platform").description("장비 플랫폼"),
                                fieldWithPath("social").description("소셜 계정 플랫폼"),
                                fieldWithPath("accessToken").description("소셜 계정 플랫폼 엑세스 토큰"),
                                fieldWithPath("deviceToken").description("FCM 디바이스 토큰")
                        ),
                        responseFields(
                                fieldWithPath("uid").description("사용자 ID"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("accessToken").description("엑세스 토큰"),
                                fieldWithPath("refreshToken").description("리프레쉬 토큰")
                        )
                ));
    }

    @DisplayName("엑세스 토큰 갱신")
    @Test
    public void 토큰_갱신() throws Exception {
        String body = objectMapper.writeValueAsString(
                new RefreshTokenRequest(
                        "eyJraWQiOiJrZXkxIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiIxIiwiaWF0IjoxNjQ4ODk1MzIwLCJleHAiOjE2NjQ0NDczMjB9.5CdG1M9PgRYb7Cbtyd5jycMJV3_CrtamPyIwfHv9Jo1xln8zsSKqsDBfVPO1F_d3o_McS2TlcTvXLMCMSVtWPA"
                ));

        mockMvc.perform(
                post("/api/auth/refresh")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("auth/refresh",
                        requestFields(
                                fieldWithPath("refreshToken").description("리프레쉬 토큰")),
                        responseFields(
                                fieldWithPath("accessToken").description("엑세스 토큰")
                        )
                ));
    }
}
