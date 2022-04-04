package me.blindcafe.blindcafe.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReasonType {

    FOR_LEAVE_ROOM("방 나가는 이유"),
    FOR_REPORT("신고 이유"),
    FOR_RETIRED("탈퇴 이유");
    
    private final String description;
}
