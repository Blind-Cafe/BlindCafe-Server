package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.notice.Notice;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticeListResponse {

    private Page<NoticeInfo> notices;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class NoticeInfo {
        private Long noticeId;
        private String title;
        private String content;
        private LocalDateTime createdAt;

        public static NoticeInfo fromEntity(Notice notice) {
            return NoticeInfo.builder()
                    .noticeId(notice.getId())
                    .title(notice.getTitle())
                    .content(notice.getContent())
                    .createdAt(notice.getCreatedAt())
                    .build();
        }
    }
}
