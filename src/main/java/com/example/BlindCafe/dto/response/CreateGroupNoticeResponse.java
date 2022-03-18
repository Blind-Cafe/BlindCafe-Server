package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.notice.GroupNotice;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupNoticeResponse {
    private String title;
    private String content;

    public static CreateGroupNoticeResponse fromEntity(GroupNotice notice) {
        return CreateGroupNoticeResponse.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .build();
    }
}
