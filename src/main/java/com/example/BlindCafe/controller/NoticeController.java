package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.CreateNoticeRequest;
import com.example.BlindCafe.dto.response.NoticeListResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static com.example.BlindCafe.config.SecurityConfig.getUid;
import static com.example.BlindCafe.config.SecurityConfig.isAdmin;
import static com.example.BlindCafe.exception.CodeAndMessage.FORBIDDEN_AUTHORIZATION;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지 조회하기
     */
    @GetMapping()
    public ResponseEntity<NoticeListResponse> getGroupNotices(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "-1") int page,
            @RequestParam(value = "offset", defaultValue = "0") Long offset
    ) {
        log.info("GET /api/notice");
        return ResponseEntity.ok(noticeService.getGroupNotices(getUid(authentication), page, offset));
    }

    /**
     * 공지 작성하기
     */
    @PostMapping()
    public ResponseEntity<Void> writeNotice(
            Authentication authentication,
            CreateNoticeRequest request
    ) {
        log.info("POST /api/notice");
        if (!isAdmin(authentication))
            throw new BlindCafeException(FORBIDDEN_AUTHORIZATION);

        if (Objects.isNull(request.getUserId())) noticeService.createNotice(request);
        else noticeService.createNotice(request, request.getUserId());
        return ResponseEntity.ok().build();
    }
}
