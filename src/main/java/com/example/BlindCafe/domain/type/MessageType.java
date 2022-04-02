package com.example.BlindCafe.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {

    TEXT(true, "1", null, null, false, true, true),
    IMAGE(true, "2", null, "사진이 전송되었습니다.", true, true, true),
    AUDIO(true, "3", null, "소리가 전송되었습니다.", false, true, true),
    VIDEO(true, "4", null, "영상이 전송되었습니다.", false, true, true),
    EXCHANGE_PROFILE(true, "5", "💓대화 종료💓", "3일간 대화가 종료됐습니다. 프로필을 교환해 대화를 이어가세요!", false, true, true),
    PROFILE(true, "6", "💌프로필 전송💌", "상대방의 프로필이 도착했습니다!", false, true, true),

    MATCHING(false, "10", "💘매칭 성공💘", "매칭에 성공했습니다. 어서 확인해보세요!", false, true, true),
    DRINK(false, "11", "☕음료 주문☕", "상대방이 음료를 주문하고 테이블에 착석했습니다!", false, true, true),
    GRANT_IMAGE(false, "12", "💌2일차 기능 해제💌", "24시간이 지나 사진을 전송할 수 있습니다. 확인해보세요!", false, true, true),
    GRANT_VOICE(false, "13", "🔈3일차 기능 해제🔈", "48시간이 지나 음성을 전송할 수 있습니다. 확인해보세요!", false, true, true),
    END_OF_ONE_HOUR(false, "14", "💗마지막 대화💗", "잠시 후 이 대화방은 닫힙니다. 그동안 못다한 말을 해보세요.", false, true, true),
    SUCCESS_EXCHANGE(true, "15", "💘프로필 교환 성공💘", "프로필 교환에 성공했습니다. 축하드려요!", false, true, true),
    LAST_CHAT(false, "16", "💗마지막 대화💗", "내일 이 대화방은 닫힙니다. 그동안 못다한 말을 해보세요", false, true, true),
    MATCHING_UP(false, "17", "⏳매칭 요청 급상승 중⏳", "현재 사용자들의 매칭 요청이 증가하고 있어요. 매칭을 요청하세요!", false, true, true),
    TAKE_DRINK(false, "18", null, "음료수 뱃지를 획득했습니다.", false, false, false),
    LEAVE(false, "19", null, "방을 나가셨습니다.", false, false, false),

    TEXT_TOPIC(true, "20", "💌토픽 전송💌", "토픽이 전송되었습니다.", false, true, false),
    IMAGE_TOPIC(true, "21", "💌토픽 전송💌", "토픽이 전송되었습니다.", false, true, false),
    AUDIO_TOPIC(true, "22", "💌토픽 전송💌", "토픽이 전송되었습니다.", false, true, false);

    private final boolean inChat;
    private final String type;
    private final String title;
    private final String body;
    private final boolean image;
    private final boolean notification;
    private final boolean isPublish;
}
