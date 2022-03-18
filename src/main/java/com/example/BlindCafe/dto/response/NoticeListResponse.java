package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.notice.Notice;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeListResponse {

    private List<NoticeInfo> notices;

    @Builder
    public static class NoticeInfo {
        private Long noticeId;
        private String title;
        private LocalDateTime createdAt;

        public static NoticeInfo fromEntity(Notice notice) {
            return NoticeInfo.builder()
                    .noticeId(notice.getId())
                    .title(notice.getTitle())
                    .createdAt(notice.getCreatedAt())
                    .build();
        }
    }
}
