package me.blindcafe.blindcafe.restdocs;

import me.blindcafe.blindcafe.domain.type.Gender;
import me.blindcafe.blindcafe.domain.type.Mbti;
import me.blindcafe.blindcafe.dto.request.*;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserApiDocumentTest extends ApiDocumentTest {

    @DisplayName("전화번호 중복 확인")
    @Test
    public void 전화번호_중복_확인() throws Exception {
        String body = objectMapper.writeValueAsString(new PhoneCheckRequest("010-1234-5678"));

        mockMvc.perform(
                post("/api/user/phone-check")
                    .header("Authorization", token)
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/duplicate-phone",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("phone").description("전화 번호")),
                        responseFields(
                                fieldWithPath("status").description("사용 가능 유무")
                        )
                ));
    }


    @DisplayName("유저 추가 정보 입력")
    @Ignore
    public void 유저_추가_정보_입력() throws Exception {
        Long[] interest = {1L,2L,3L};
        String body = objectMapper.writeValueAsString(new AddUserInfoRequest(
                25, Gender.M, "010-1234-5678", "골목대장김희동", Gender.F, Arrays.asList(interest)
        ));

        mockMvc.perform(
                post("/api/user")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/add-user-info",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("age").description("나이"),
                                fieldWithPath("myGender").description("본인 성별"),
                                fieldWithPath("phone").description("전화 번호"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("partnerGender").description("상대방 성별"),
                                fieldWithPath("interests").description("관심사 리스트")
                        )));
    }

    @DisplayName("마이 페이지")
    @Test
    public void 마이페이지() throws Exception {
        mockMvc.perform(
                get("/api/user")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/my-page",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        responseFields(
                                fieldWithPath("avatar").description("프로필 이미지"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("myGender").description("본인 성별"),
                                fieldWithPath("partnerGender").description("상대방 성별"),
                                fieldWithPath("age").description("나이"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("mbti").description("MBTI"),
                                fieldWithPath("voice").description("목소리"),
                                fieldWithPath("interests").description("관심사 리스트"),
                                fieldWithPath("drinks").description("음료수 뱃지 리스트")
                        )
                ));
    }

    @DisplayName("프로필 수정")
    @Test
    public void 프로필_수정() throws Exception {
        String body = objectMapper.writeValueAsString(
                new UpdateProfileRequest(
                        "서울", "종로구", Gender.F, Mbti.ENFJ
                ));

        mockMvc.perform(
                put("/api/user")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/edit-profile",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("state").description("지역(시/도)"),
                                fieldWithPath("region").description("지역(구/군)"),
                                fieldWithPath("partnerGender").description("상대방 성별"),
                                fieldWithPath("mbti").description("MBTI")),
                        responseFields(
                                fieldWithPath("avatar").description("프로필 이미지"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("phone").description("전화번호"),
                                fieldWithPath("myGender").description("본인 성별"),
                                fieldWithPath("partnerGender").description("상대방 성별"),
                                fieldWithPath("age").description("나이"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("mbti").description("MBTI"),
                                fieldWithPath("voice").description("목소리"),
                                fieldWithPath("interests").description("관심사 리스트"),
                                fieldWithPath("drinks").description("음료수 뱃지 리스트"))
                        ));
    }

    @DisplayName("관심사 수정")
    @Test
    public void 관심사_수정() throws Exception {
        Long[] interests = {1L,2L,3L};
        String body = objectMapper.writeValueAsString(
                new UpdateInterestRequest(
                        Arrays.asList(interests)
                ));

        mockMvc.perform(
                put("/api/user/interest")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/edit-interest",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("interests").description("관심사 리스트")
                        )));
    }

    @DisplayName("프로필 이미지 리스트 조회")
    @Test
    public void 프로필_이미지_리스트_조회() throws Exception {
        mockMvc.perform(
                get("/api/user/{id}/avatar", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/avatar-list",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        pathParameters(
                                parameterWithName("id").description("유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("avatars").description("프로필 이미지 리스트")
                        )
                ));
    }

    @DisplayName("프로필 이미지 업로드/수정")
    @Test
    public void 프로필_이미지_업로드_수정() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "image.png", "image/png", "<<png data>>".getBytes());

        mockMvc.perform(
                fileUpload("/api/user/avatar")
                        .file(image)
                        .param("sequence", "1")
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/avatar-upload",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("sequence").description("프로필 이미지 순서")
                        ),
                        requestParts(
                                partWithName("image").description("프로필 이미지")
                        )
                ));
    }

    @DisplayName("프로필 이미지 삭제")
    @Test
    public void 프로필_이미지_삭제() throws Exception {
        mockMvc.perform(
                delete("/api/user/avatar")
                        .header("Authorization", token)
                        .param("seq", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/avatar-delete",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("seq").description("프로필 이미지 순서")
                        )
                ));
    }

    @DisplayName("사용자 목소리 설정")
    @Test
    public void 사용자_목소리_설정() throws Exception {
        MockMultipartFile voice = new MockMultipartFile("voice", "voice.mp3", "audio/mpeg", "<<mp3 data>>".getBytes());

        mockMvc.perform(
                fileUpload("/api/user/voice")
                        .file(voice)
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/voice-upload",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParts(
                                partWithName("voice").description("음성 파일")
                        )
                ));
    }

    @DisplayName("사용자 목소리 삭제")
    @Test
    public void 사용자_목소리_삭제() throws Exception {
        mockMvc.perform(
                delete("/api/user/voice")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/voice-delete",
                        requestHeaders(headerWithName("Authorization").description("JWT"))
                ));
    }

    @DisplayName("사용자 탈퇴")
    @Test
    public void 사용자_탈퇴() throws Exception {
        mockMvc.perform(
                delete("/api/user")
                        .header("Authorization", token)
                        .param("reason", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/retire",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("reason").description("탈퇴 사유 ID")
                        ),
                        responseFields(
                                fieldWithPath("nickname").description("탈퇴 회원 닉네임")
                        )
                ));
    }

    @DisplayName("상대방_프로필_조회")
    @Test
    public void 상대방_프로필_조회() throws Exception {
        mockMvc.perform(
                get("/api/user/{id}/profile", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/profile",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        pathParameters(
                                parameterWithName("id").description("유저 ID")
                        ),
                        responseFields(
                                fieldWithPath("avatars").description("프로필 이미지 리스트"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("age").description("나이"),
                                fieldWithPath("address").description("주소"),
                                fieldWithPath("mbti").description("MBTI"),
                                fieldWithPath("voice").description("목소리"),
                                fieldWithPath("interests").description("관심사 리스트")
                        )
                ));
    }

    @DisplayName("건의사항 작성")
    @Test
    public void 건의사항_작성() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "image.png", "image/png", "<<png data>>".getBytes());

        mockMvc.perform(
                fileUpload("/api/user/suggestion")
                        .file(image)
                        .param("content", "건의사항 내용")
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/suggestion",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("content").description("건의사항 내용")
                        ),
                        requestParts(
                                partWithName("images").description("건의사항 첨부 이미지")
                        )
                ));
    }

    @DisplayName("사용자 신고")
    @Test
    public void 사용자_신고() throws Exception {
        String body = objectMapper.writeValueAsString(
                new ReportRequest(
                        2L, 1L
                ));

        mockMvc.perform(
                post("/api/user/report")
                        .header("Authorization", token)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/report-create",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestFields(
                                fieldWithPath("matchingId").description("매칭 아이디"),
                                fieldWithPath("reason").description("신고 사유 ID")
                        )));

    }

    @DisplayName("신고 내역 조회")
    @Test
    public void 신고_내역_조회() throws Exception {
        mockMvc.perform(
                get("/api/user/report")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user/report-select",
                        requestHeaders(headerWithName("Authorization").description("JWT")),
                        requestParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("조회할 건수")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("reports").description("신고 내역"),
                                fieldWithPath("reports.content[0].date").description("신고 일시 (YYYY-MM-DD)"),
                                fieldWithPath("reports.content[0].target").description("신고 대상"),
                                fieldWithPath("reports.content[0].reason").description("신고 사유")
                        )
                ));
    }
}
