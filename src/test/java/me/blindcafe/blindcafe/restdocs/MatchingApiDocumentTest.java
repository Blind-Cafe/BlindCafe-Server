package me.blindcafe.blindcafe.restdocs;

import me.blindcafe.blindcafe.dto.request.ExchangeProfileRequest;
import me.blindcafe.blindcafe.dto.request.SelectDrinkRequest;
import me.blindcafe.blindcafe.dto.request.TopicRequest;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MatchingApiDocumentTest extends ApiDocumentTest {

    @DisplayName("매칭 요청")
    @Test
    public void 매칭_요청() throws Exception {
        mockMvc.perform(
                post("/api/matching")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/request",
                        requestHeaders(headerWithName("Authorization").description("JWT"))
                ));
    }

    @DisplayName("매칭 요청 취소")
    @Ignore
    public void 매칭_요청_취소() throws Exception {
        mockMvc.perform(
                delete("/api/matching")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/cancel",
                        requestHeaders(headerWithName("Authorization").description("JWT"))
                ));
    }

    @DisplayName("채팅방 리스트 조회")
    @Test
    public void 채팅방_리스트_조회() throws Exception {
        mockMvc.perform(
                get("/api/matching")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/list",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        relaxedResponseFields(
                                fieldWithPath("request").description("매칭 요청 진행 여부"),
                                
                                fieldWithPath("blind").description("3일 채팅방"),
                                fieldWithPath("blind[0].matchingId").description("매칭 ID"),
                                fieldWithPath("blind[0].partner").description("상대방 정보"),
                                fieldWithPath("blind[0].partner.userId").description("상대방 사용자 ID"),
                                fieldWithPath("blind[0].partner.nickname").description("상대방 닉네임"),
                                fieldWithPath("blind[0].partner.avatar").description("상대방 프로필 이미지"),
                                fieldWithPath("blind[0].latestMessage").description("최근 메시지"),
                                fieldWithPath("blind[0].received").description("최근 메시지 조회 여부"),
                                fieldWithPath("blind[0].blind").description("3일 채팅방 여부"),
                                fieldWithPath("blind[0].expiredDt").description("만료 시간"),
                                
                                fieldWithPath("bright").description("7일 채팅방")
//                                fieldWithPath("bright[0].matchingId").description("매칭 ID"),
//                                fieldWithPath("bright[0].partner").description("상대방 정보"),
//                                fieldWithPath("bright[0].partner.userId").description("상대방 사용자 ID"),
//                                fieldWithPath("bright[0].partner.nickname").description("상대방 닉네임"),
//                                fieldWithPath("bright[0].partner.avatar").description("상대방 프로필 이미지"),
//                                fieldWithPath("bright[0].latestMessage").description("최근 메시지"),
//                                fieldWithPath("bright[0].received").description("최근 메시지 조회 여부"),
//                                fieldWithPath("bright[0].blind").description("3일 채팅방 여부"),
//                                fieldWithPath("bright[0].expiredDt").description("만료 시간")
                        )
                ));
    }

    @DisplayName("채팅방 정보 조회")
    @Test
    public void 채팅방_정보_조회() throws Exception {
        mockMvc.perform(
                get("/api/matching/{id}", 2L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/info",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        pathParameters(
                                parameterWithName("id").description("매칭 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("matchingId").description("매칭 ID"),
                                fieldWithPath("partner").description("상대방 정보"),
                                fieldWithPath("partner.userId").description("상대방 사용자 ID"),
                                fieldWithPath("partner.nickname").description("상대방 닉네임"),
                                fieldWithPath("partner.avatar").description("상대방 프로필 이미지"),
                                fieldWithPath("selectedDrink").description("음료 선택 여부"),
                                fieldWithPath("beginDt").description("시작일"),
                                fieldWithPath("expiredDt").description("종료일"),
                                fieldWithPath("topic").description("토픽 정보"),
                                fieldWithPath("topic.topicId").description("토픽 ID"),
                                fieldWithPath("topic.type").description("토픽 타입"),
                                fieldWithPath("topic.text").description("토픽 내용 (TEXT 타입 아닌 경우 null)"),
                                fieldWithPath("topic.title").description("토픽 제목 (AUDIO, IMAGE 타입 아닌 경우 null)"),
                                fieldWithPath("topic.src").description("토픽 소스 (AUDIO, IMAGE 타입 아닌 경우 null)"),
                                fieldWithPath("topic.access").description("최근 토픽 조회 시간"),
                                fieldWithPath("continuous").description("7일 채팅방 여부"),
                                fieldWithPath("active").description("채팅방 활성화 여부")
                        )
                ));
    }

    @DisplayName("음료수 선택")
    @Test
    public void 음료수_선택() throws Exception {
        String body = objectMapper.writeValueAsString(
                new SelectDrinkRequest(
                        2L, 1L
                )
        );
        mockMvc.perform(
                post("/api/matching/drink")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/drink",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("matchingId").description("매칭 ID"),
                                fieldWithPath("drink").description("음료 ID")
                        )
                ));
    }

    @DisplayName("토픽_조회")
    @Test
    public void 토픽_조회() throws Exception {
        String body = objectMapper.writeValueAsString(new TopicRequest(2L));

        mockMvc.perform(
                post("/api/matching/topic")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/topic",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("matchingId").description("매칭 ID")
                        )
                ));
    }

    @DisplayName("프로필 교환")
    @Test
    public void 프로필_교환() throws Exception {
        String body = objectMapper.writeValueAsString(new ExchangeProfileRequest(2L));

        mockMvc.perform(
                post("/api/matching/exchange")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/exchange",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("matchingId").description("매칭 ID")
                        )
                ));
    }

    @DisplayName("채팅방 나가기")
    @Test
    public void 채팅방_나가기() throws Exception {
        mockMvc.perform(
                delete("/api/matching/{id}", 2L)
                        .header("Authorization", token)
                        .param("reason", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("matching/leave",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        pathParameters(
                                parameterWithName("id").description("매칭 ID")
                        ),
                        requestParameters(
                                parameterWithName("reason").description("탈퇴 사유 ID")
                        )
                ));
    }
}
