package me.blindcafe.blindcafe.domain;

import me.blindcafe.blindcafe.utils.DateTimeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_connect")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyConnect {

    @Id
    @Column(name = "daily_connect_id")
    private String day;

    private Long entireCount;

    private Long maleCount;

    private Long femaleCount;

    public static DailyConnect create(LocalDateTime time, Long entire, Long male) {
        DailyConnect dc = new DailyConnect();
        dc.setDay(time.format(DateTimeUtil.dateFormatter));
        dc.setEntireCount(entire);
        dc.setMaleCount(male);
        dc.setFemaleCount(entire - male);
        return dc;
    }
}
