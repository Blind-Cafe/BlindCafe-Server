package com.example.BlindCafe.service;

import com.example.BlindCafe.config.jwt.JwtProperties;
import com.example.BlindCafe.config.jwt.JwtUtils;
import com.example.BlindCafe.domain.DailyConnect;
import com.example.BlindCafe.dto.request.AdminLoginRequest;
import com.example.BlindCafe.dto.response.WeeklyMemberStateResponse;
import com.example.BlindCafe.repository.DailyConnectRepository;
import com.example.BlindCafe.repository.ReportRepository;
import com.example.BlindCafe.repository.SuggestionRepository;
import com.example.BlindCafe.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.config.SecurityConfig.ADMIN_ID;
import static com.example.BlindCafe.config.SecurityConfig.ADMIN_PW;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final SuggestionRepository suggestionRepository;
    private final DailyConnectRepository dailyConnectRepository;

    // 관리자 로그인
    public String login(AdminLoginRequest request) {
        if (request.getId().equals(ADMIN_ID) && request.getPassword().equals(ADMIN_PW)) {
            return JwtUtils.createWebAccessToken(0L);
        }
        return null;
    }

    // 관리자 로그인 토큰 검증
    public boolean isValidAdmin(String token) {
        String id = JwtUtils.getUsedId(JwtProperties.AUTHORIZATION_TYPE + token);
        return id.equals("0");
    }

    // 처리 안한 신고 내역 수
    public Long getUncheckedReportCount() {
        return reportRepository.countUncheckedReport();
    }

    // 처리 안한 건의 사항 수
    public Long getUncheckedSuggestionCount() {
        return suggestionRepository.countUncheckedSuggestion();
    }

    // 주간 접속자, 주간 접속자 비율
    public WeeklyMemberStateResponse getWeeklyMemberState() {
        LocalDateTime today = LocalDateTime.now();
        String begin;
        String end;

        // 매일 밤 새벽 2시에 배치 작업이 진행
        // 0시 ~ 2시 -> 이틀 전 기준
        if (today.format(DateTimeUtil.timeFormatter).compareTo("03:00:00") < 0) {
            begin = today.minusDays(8L).format(DateTimeUtil.dateFormatter);
            end = today.minusDays(2L).format(DateTimeUtil.dateFormatter);
        } else {
            begin = today.minusDays(7L).format(DateTimeUtil.dateFormatter);
            end = today.minusDays(1L).format(DateTimeUtil.dateFormatter);
        }

        // 주간 사용량 조회
        List<DailyConnect> weeklyState = dailyConnectRepository.getWeeklyState(begin, end);
        List<Long> weekly = weeklyState.stream()
                .map(DailyConnect::getEntireCount)
                .collect(Collectors.toList());
        Long male = 0L;
        Long female = 0L;
        for (DailyConnect w: weeklyState) {
            male += w.getMaleCount();
            female += w.getFemaleCount();
        }

        return new WeeklyMemberStateResponse(weekly, male, female);
    }
}
