package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.status.CommonStatus;
import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reported {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reported_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(STRING)
    @Column(length = 10, nullable = false)
    private CommonStatus status;
}
