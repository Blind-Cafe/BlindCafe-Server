package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.status.ReportStatus;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "report_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "matching_id")
    private Matching matching;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User plaintiff;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reported_id")
    private Reported defendant;

    private String reason;

    @Enumerated(STRING)
    private ReportStatus status;

}
