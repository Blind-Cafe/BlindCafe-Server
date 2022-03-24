package com.example.BlindCafe.dto.response;

import com.example.BlindCafe.domain.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReportListResponse {

    private List<ReportDto> reports;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReportDto {
        private String date;
        private String target;
        private String reason;

        public static ReportDto fromEntity(Report report) {
            ReportDto reportDto = new ReportDto();
            reportDto.setDate(report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            reportDto.setTarget(report.getReported().getNickname());
            reportDto.setReason(report.getReason().getText());
            return reportDto;
        }
    }
}
