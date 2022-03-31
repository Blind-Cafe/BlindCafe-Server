package com.example.BlindCafe.restdocs;

import com.example.BlindCafe.ApiDocumentTest;
import com.example.BlindCafe.dto.request.PhoneCheckRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserApiDocumentTest extends ApiDocumentTest {

    private static final String JWT = "eyJraWQiOiJrZXkyIiwiYWxnIjoiSFM1MTIifQ.eyJzdWIiOiIxIiwiaWF0IjoxNjQ4NjE5NTg3LCJleHAiOjE2NDk4MjkxODd9.dh6eesIiJnh-odjxTLxQvFEiukftD4IyzPEgUypTx_Yw43Gna3miYkfQDAFkQXBwRWWwNaoETpQWBfMDR-6qAA";

    @DisplayName("전화번호 중복 확인")
    @Test
    public void 전화번호_중복_확인() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(new PhoneCheckRequest("010-1234-5678"));

        // when
        // then
        mockMvc.perform(
                post("/api/user/phone-check")
                    .header("Authorization", JWT)
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andDo(document("user/duplicate-phone",
                        requestHeaders(headerWithName("Authorization").description("jwt login header")),
                        requestFields(
                                fieldWithPath("phone").description("전화 번호")
                        )))
                .andExpect(status().isOk());
    }
    
    @DisplayName("마이 페이지")
    @Test
    public void 마이페이지() throws Exception {
        // given
        // user save
        // when
        // then

        mockMvc.perform(
                get("/api/user")
                        .header("Authorization", JWT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 유저 정보 추가 입력
     * 프로필 수정
     * 관심사 수정
     * 프로필 이미지 리스트 조회
     * 프로필 이미지 업로드/수정
     * 프로필 이미지 삭제
     * 사용자 목소리 설정하기
     * 사용자 목소리 삭제하기
     * 유저 탈퇴
     * 프로필(상대방) 조회
     * 건의사항 작성하기
     * 신고하기
     * 신고 내역 조회하기
     */
}
