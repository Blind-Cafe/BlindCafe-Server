package me.blindcafe.blindcafe.dto.response;

import me.blindcafe.blindcafe.domain.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
public class ReportListResponse {

    private Page<ReportDto> reports;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReportDto {
        private String date;
        private String target;
        private String reason;

        public static ReportDto fromEntity(Report report) {
            ReportDto reportDto = new ReportDto();
            reportDto.setDate(report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            reportDto.setTarget(report.getReported().getNickname());
            reportDto.setReason(report.getReason().getText());
            return reportDto;
        }
    }
}
