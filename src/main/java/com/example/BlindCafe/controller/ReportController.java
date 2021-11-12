package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.CreateReportDto;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.example.BlindCafe.config.SecurityConfig.getUserId;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고하기
     */
    @PostMapping
    public CreateReportDto.Response addUserInfo(
            Authentication authentication,
            @Valid @RequestBody CreateReportDto.Request request
    ) {
        log.info("POST /api/report");
        return reportService.createReport(getUserId(authentication), request);
    }

}
