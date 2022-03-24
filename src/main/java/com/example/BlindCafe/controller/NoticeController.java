package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.CreateNoticeRequest;
import com.example.BlindCafe.dto.response.NoticeListResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static com.example.BlindCafe.config.jwt.JwtAuthorizationFilter.ADMIN;
import static com.example.BlindCafe.config.jwt.JwtAuthorizationFilter.UID;
import static com.example.BlindCafe.exception.CodeAndMessage.FORBIDDEN_AUTHORIZATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지 조회하기
     */
    @GetMapping("/notice")
    public ResponseEntity<NoticeListResponse> getGroupNotices(
            @RequestHeader(value = UID) String uid,
            @RequestParam(value = "page", defaultValue = "-1") int page,
            @RequestParam(value = "offset", defaultValue = "0") Long offset
    ) {
        log.info("GET /api/notice");
        return ResponseEntity.ok(noticeService.getGroupNotices(Long.parseLong(uid), page, offset));
    }

    /**
     * 공지 작성하기
     */
    @PostMapping("/notice")
    public ResponseEntity<Void> writeNotice(
            @RequestHeader(value = ADMIN) String admin,
            CreateNoticeRequest request
    ) {
        log.info("POST /api/notice");
        if (!admin.equals(ADMIN))
            throw new BlindCafeException(FORBIDDEN_AUTHORIZATION);

        if (Objects.isNull(request.getUserId())) noticeService.createNotice(request);
        else noticeService.createNotice(request, request.getUserId());
        return ResponseEntity.ok().build();
    }
}
