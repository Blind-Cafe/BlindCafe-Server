package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.NoticeLog;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.notice.GroupNotice;
import com.example.BlindCafe.domain.notice.Notice;
import com.example.BlindCafe.domain.notice.PersonalNotice;
import com.example.BlindCafe.dto.request.CreateNoticeRequest;
import com.example.BlindCafe.dto.response.NoticeListResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.NoticeLogRepository;
import com.example.BlindCafe.repository.NoticeRepository;
import com.example.BlindCafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.EMPTY_USER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeLogRepository noticeLogRepository;

    private final int NOTICE_PAGE_SIZE = 25;

    /**
     * 공지 조회하기 + 공지 화면 접속 로그 남기기
     */
    @Transactional
    public NoticeListResponse getGroupNotices(Long userId, int page, Long offset) {
        List<Notice> notices = new ArrayList<>();
        if (page > -1) {
            // page 기준으로 공지 조회
            notices = noticeRepository.findGroupNoticeByPage(NOTICE_PAGE_SIZE, page * NOTICE_PAGE_SIZE);
        } else {
            notices = noticeRepository.findGroupNoticeByOffset(NOTICE_PAGE_SIZE, offset);
        }

        // 공지 화면 접속 로그 저장
        updateNoticeLog(userId);

        return new NoticeListResponse(notices.stream()
                        .map(NoticeListResponse.NoticeInfo::fromEntity)
                        .collect(Collectors.toList()));
    }

    // 공지 화면 접속 기록 저장
    private void updateNoticeLog(Long uid) {
        LocalDateTime now = LocalDateTime.now();
        NoticeLog noticeLog = noticeLogRepository.findByUserId(uid).orElse(null);
        if (Objects.isNull(noticeLog)) {
            NoticeLog newLog = NoticeLog.create(uid, now);
            noticeLogRepository.save(newLog);
            return;
        }
        noticeLog.update(now);
    }

    /**
     * 그룹 공지 작성하기
     */
    @Transactional
    public void createNotice(CreateNoticeRequest request) {
        GroupNotice notice = GroupNotice.create(request.getTitle(), request.getContent());
        noticeRepository.save(notice);
    }

    /**
     * 개인 공지(쪽지) 작성하기
     */
    @Transactional
    public void createNotice(CreateNoticeRequest request, Long uid) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        PersonalNotice notice = PersonalNotice.create(request.getTitle(), request.getContent(), user);
        noticeRepository.save(notice);
    }

    /**
     * 미수신 공지 조회
     */
    public boolean isUnreceivedNotice(Long uid) {

        // 최근 공지 조회
        Optional<Notice> latestNoticeOptional = noticeRepository.findFirstByOrderByCreatedAtDesc();
        if (latestNoticeOptional.isEmpty())
            return false;

        // 최근 공지와 공지 화면 접속 기록 비교
        LocalDateTime latestNoticeDt = latestNoticeOptional.get().getCreatedAt();
        NoticeLog log = noticeLogRepository.findByUserId(uid)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return latestNoticeDt.isAfter(log.getAccessDt());
    }
}
