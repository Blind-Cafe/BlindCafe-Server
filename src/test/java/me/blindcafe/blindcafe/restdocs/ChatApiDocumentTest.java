package me.blindcafe.blindcafe.restdocs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatApiDocumentTest extends ApiDocumentTest {

    @DisplayName("파일 전송")
    @Test
    public void 파일_전송() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "<<png data>>".getBytes());

        mockMvc.perform(
                fileUpload("/api/chat/matching")
                        .file(file)
                        .param("matchingId", "2")
                        .param("senderId", "1")
                        .param("senderName", "골목대장김희동")
                        .param("type", "1")
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("chat/send-file",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("matchingId").description("매칭 ID"),
                                parameterWithName("senderId").description("전송한 사용자 ID"),
                                parameterWithName("senderName").description("전송한 사용자 닉네임"),
                                parameterWithName("type").description("메시지 타입")
                        ),
                        requestParts(
                                partWithName("file").description("전송한 파일")
                        )
                ));
    }

    @DisplayName("메시지 조회")
    @Test
    public void 메시지_조회() throws Exception {
        mockMvc.perform(
                get("/api/chat/matching/{id}", "2")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("chat/message",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        pathParameters(
                                parameterWithName("id").description("매칭 ID")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("조회할 건수")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("messages").description("신고 내역"),
                                fieldWithPath("messages.content[0].messageId").description("메시지 ID"),
                                fieldWithPath("messages.content[0].userId").description("전송한 사용자 ID"),
                                fieldWithPath("messages.content[0].type").description("메시지 타입"),
                                fieldWithPath("messages.content[0].content").description("메시지 내용"),
                                fieldWithPath("messages.content[0].createdAt").description("메시지 전송 일시")
                        )
                ));
    }
}
