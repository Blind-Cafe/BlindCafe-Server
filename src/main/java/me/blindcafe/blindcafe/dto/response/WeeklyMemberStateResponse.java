package me.blindcafe.blindcafe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyMemberStateResponse {
    private List<Long> weekly;
    private Long male;
    private Long female;
}
