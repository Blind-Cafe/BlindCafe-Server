package me.blindcafe.blindcafe.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported")
    private User reported;

    private Long matchingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id")
    private Reason reason;

    private boolean isChecked;

    public void setReporter(User reporter) {
        this.reporter = reporter;
        reporter.getMyReport().add(this);
    }

    public void setReported(User reported) {
        this.reported = reported;
        reported.getReported().add(this);
        if (reported.getReported().size() >= 10)
            reported.suspend();
    }

    public static Report create(User reporter, User reported, Long matchingId , Reason reason) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setMatchingId(matchingId);
        report.setReason(reason);
        report.setChecked(false);
        return report;
    }
}
