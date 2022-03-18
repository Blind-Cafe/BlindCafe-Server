package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.Report;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReportListDto {

    private List<ReportDto> reports;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ReportDto {
        private String date;
        private String target;
        private String reason;
        private String status;

        public ReportDto(Report report) {
            this.date = report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            this.target = report.getReported().getNickname();
            this.reason = report.getReason().getText();
            this.status = report.getStatus().getDescription();
        }
    }
}
