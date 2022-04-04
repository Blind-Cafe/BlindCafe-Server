package me.blindcafe.blindcafe.controller;

import me.blindcafe.blindcafe.dto.request.CreateNoticeRequest;
import me.blindcafe.blindcafe.dto.response.NoticeListResponse;
import me.blindcafe.blindcafe.exception.BlindCafeException;
import me.blindcafe.blindcafe.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static me.blindcafe.blindcafe.config.SecurityConfig.getUid;
import static me.blindcafe.blindcafe.config.SecurityConfig.isAdmin;
import static me.blindcafe.blindcafe.exception.CodeAndMessage.FORBIDDEN_AUTHORIZATION;

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
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        log.info("GET /api/notice");
        return ResponseEntity.ok(noticeService.getGroupNotices(getUid(authentication), page, size));
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
