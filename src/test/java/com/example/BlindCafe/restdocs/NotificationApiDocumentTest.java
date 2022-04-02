package com.example.BlindCafe.restdocs;

import com.example.BlindCafe.dto.request.NotificationSettingRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationApiDocumentTest extends ApiDocumentTest {

    @DisplayName("알림 설정 변경")
    @Test
    public void 알림_설정_변경() throws Exception {
        String body = objectMapper.writeValueAsString(new NotificationSettingRequest(0L, true));

        mockMvc.perform(
                put("/api/notification")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("notification/setting",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("target").description("매칭 ID (0일 경우 전체 설정)"),
                                fieldWithPath("active").description("알림 활성화 여부"))
                ));
    }
}
