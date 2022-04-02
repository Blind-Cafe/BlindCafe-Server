package com.example.BlindCafe.restdocs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MainApiDocumentTest extends ApiDocumentTest {

    @DisplayName("홈")
    @Test
    public void 홈() throws Exception {
        mockMvc.perform(
                get("/api/main")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("main/home",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        responseFields(
                                fieldWithPath("request").description("매칭 요청 상태 여부"),
                                fieldWithPath("tickets").description("현재 가지고 있는 매칭권 수"),
                                fieldWithPath("notice").description("최근 작성된 공지 여부")
                        )
                ));
    }
}
