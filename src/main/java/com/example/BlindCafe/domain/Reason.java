package com.example.BlindCafe.domain;

import com.example.BlindCafe.domain.type.ReasonType;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reason {

    @Id
    @GeneratedValue
    @Column(name = "reason_id")
    private Long id;

    private Long num;

    private String text;

    @Enumerated(STRING)
    private ReasonType reasonType;
}
