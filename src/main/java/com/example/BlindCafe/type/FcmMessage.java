package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmMessage {
    MATCHING("매칭 성공", "💘매칭이 성공됐습니다💘 어서 확인해보세요💨", "HOME", "F"),
    MATCHING_OPEN("대화방 오픈", "상대방이 ☕음료☕를 주문하고 테이블에 착석했습니다. 음료수를 주문해주세요!", "ROOM", "T"),
    ONE_DAY("하루 달성", "24시간이 지나 💌사진을 전송💌할 수 있습니다. 확인해보세요.", "CHAT", "T"),
    TWO_DAYS("이틀 달성", "48시간이 지나 🔈음성🔈을 전송할 수 있습니다. 확인해보세요", "CHAT", "T"),
    END_OF_ONE_HOUR("마지막 대화", "💗잠시 후 이 대화방은 닫힙니다. 그동안 못다한 말을 해보세요💗", "CHAT", "T"),
    THREE_DAYS("대화 종료", "💓3일의 대화가 종료됐습니다. 💓프로필을 교환해 대화를 이어가세요🥰", "HOME", "F"),
    PROFILE_OPEN("상대방 프로필 도착", "💌상대방의 프로필이 도착했습니다💌", "HOME", "F"),
    MATCHING_CONTINUE("7일 대화 시작", "축하드립니다. ☕내 테이블에서 대화☕를 이어가세요", "MATCHING", "F"),
    LAST_CHAT("마지막 대화", "💗내일 이 대화방은 닫힙니다. 그동안 못다한 말을 해보세요💗", "CHAT", "T"),
    MATCHING_UP("⏳매칭 요청 급상승 중⏳", "현재 신규 유저와 매칭 요청이 증가하고 있어요. 매칭을 요청하세요!", "HOME", "F");


    private final String title;
    private final String body;
    private final String path;
    private final String type;
}
