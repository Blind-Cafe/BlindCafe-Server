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
import com.example.BlindCafe.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * 공지 조회하기 + 공지 화면 접속 로그 남기기
     */
    @Transactional
    public NoticeListResponse getGroupNotices(Long userId, int page, int size) {
        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notice> pages = noticeRepository.findBy(pageable);

        // 공지 화면 접속 로그 저장
        NoticeLog noticeLog = noticeLogRepository.findByUserId(userId).orElse(null);
        if (Objects.isNull(noticeLog)) {
            noticeLog =  NoticeLog.create(userId, now);
        }
        noticeLog.update(now);
        noticeLogRepository.save(noticeLog);

        return new NoticeListResponse(pages.map(NoticeListResponse.NoticeInfo::fromEntity));
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
        return latestNoticeDt.isAfter(DateTimeUtil.fromString(log.getAccessDt()));
    }
}
