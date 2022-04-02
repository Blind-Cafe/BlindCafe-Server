package com.example.BlindCafe.restdocs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NoticeApiDocumentTest extends ApiDocumentTest {

    @DisplayName("공지 조회")
    @Test
    public void 공지_조회() throws Exception {
        mockMvc.perform(
                get("/api/notice")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("notice/list",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("조회할 건수")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("notices").description("신고 내역"),
                                fieldWithPath("notices.content[0].noticeId").description("공지 ID"),
                                fieldWithPath("notices.content[0].title").description("공지사항 제목"),
                                fieldWithPath("notices.content[0].content").description("공지사항 내용"),
                                fieldWithPath("notices.content[0].createdAt").description("공지사항 작성 일시")
                        )
                ));
    }
}
