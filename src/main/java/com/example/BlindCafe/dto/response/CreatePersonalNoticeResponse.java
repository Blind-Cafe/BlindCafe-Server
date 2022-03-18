package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.notice.PersonalNotice;
import com.example.BlindCafe.domain.type.Platform;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePersonalNoticeResponse {
    private String title;
    private String content;
    private Long userId;
    private String deviceToken;
    private Platform platform;

    public static CreatePersonalNoticeResponse fromEntity(PersonalNotice notice) {
        return CreatePersonalNoticeResponse.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .userId(notice.getUser().getId())
                .deviceToken(notice.getUser().getDeviceToken())
                .platform(notice.getUser().getPlatform())
                .build();
    }
}
