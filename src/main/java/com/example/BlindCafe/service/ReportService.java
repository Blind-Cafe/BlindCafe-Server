package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.CreateReportDto;
import com.example.BlindCafe.dto.ReportListDto;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.ReasonType;
import com.example.BlindCafe.type.status.MatchingStatus;
import com.example.BlindCafe.type.status.ReportStatus;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.type.status.UserStatus.NORMAL;
import static com.example.BlindCafe.type.status.UserStatus.NOT_REQUIRED_INFO;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;
    private final ReasonRepository reasonRepository;
    private final ReportRepository reportRepository;

    /**
     * 신고하기
     */
    @Transactional
    public CreateReportDto.Response createReport(
            Long userId, CreateReportDto.Request request
    ) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = matchingRepository.findById(request.getMatchingId())
                .orElseThrow(() -> new BlindCafeException(NO_AUTHORIZATION_MATCHING));

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser())
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        Reason reason = reasonRepository.findByReasonTypeAndNum(ReasonType.FOR_REPORT, request.getReason())
                .orElseThrow(() -> new BlindCafeException(NO_REASON));

        Report report = Report.builder()
                .matching(matching)
                .reporter(user)
                .reported(partner)
                .reason(reason)
                .status(ReportStatus.WAIT)
                .build();
        reportRepository.save(report);

        matching.getUserMatchings().stream()
                .filter(userMatching -> userMatching.getUser().equals(user))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING))
                .setStatus(MatchingStatus.OUT);
        UserMatching partnerMatching = matching.getUserMatchings().stream()
                .filter(userMatching -> userMatching.getUser().equals(partner))
                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));
        partnerMatching.setStatus(MatchingStatus.FAILED_REPORT);
        partnerMatching.setReason(reason);

        matching.setStatus(MatchingStatus.FAILED_REPORT);

        List<Report> reporteds = reportRepository.findByReported(partner);
        if (reporteds.size() >= 5) {
            partner.setStatus(UserStatus.SUSPENDED);
            userRepository.save(partner);
        }

        return CreateReportDto.Response.builder()
                .codeAndMessage(SUCCESS).build();
    }

    public ReportListDto getReports(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        return new ReportListDto(reportRepository.findByReporter(user).stream()
                .map(ReportListDto.ReportDto::new)
                .collect(Collectors.toList()));
    }
}
