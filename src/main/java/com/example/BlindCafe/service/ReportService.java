package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.CreateReportDto;
import com.example.BlindCafe.dto.ReportListDto;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.status.ReportStatus;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
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
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Matching matching = user.getUserMatchings().stream()
                .filter(um -> um.getMatching().getId().equals(request.getMatchingId()))
                .map(um -> um.getMatching())
                .findAny()
                .orElseThrow(() -> new BlindCafeException(NO_USER_MATCHING));

        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser())
                .findAny().orElseThrow(() -> new BlindCafeException(NO_USER_MATCHING));

        Reason reason = reasonRepository.findById(request.getReason())
                .orElseThrow(() -> new BlindCafeException(NO_REASON));

        Report report = Report.builder()
                .matching(matching)
                .reporter(user)
                .reported(partner)
                .reason(reason)
                .status(ReportStatus.WAIT)
                .build();
        reportRepository.save(report);

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
