package com.example.BlindCafe.entity;

import com.example.BlindCafe.entity.type.status.ReportStatus;
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
    @JoinColumn(name = "reporter")
    private User reporter;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "reported")
    private User reported;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "reason_id")
    private Reason reason;

    @Enumerated(STRING)
    @Column(length = 10, nullable = false)
    private ReportStatus status;

}
