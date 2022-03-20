package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.request.CreateNoticeRequest;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

import static com.example.BlindCafe.config.jwt.JwtAuthorizationFilter.ADMIN;
import static com.example.BlindCafe.exception.CodeAndMessage.FORBIDDEN_AUTHORIZATION;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final NoticeService noticeService;

    @PostMapping("/notice")
    public ResponseEntity<Void> writeNotice(
            @RequestHeader(value = ADMIN) String admin,
            CreateNoticeRequest request
    ) {
        if (!admin.equals(ADMIN))
            throw new BlindCafeException(FORBIDDEN_AUTHORIZATION);

        if (Objects.isNull(request.getUserId()))
            noticeService.createNotice(request);
        else
            noticeService.createNotice(request, request.getUserId());

        return ResponseEntity.ok().build();
    }
}
