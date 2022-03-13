package com.example.BlindCafe.entity;

import com.example.BlindCafe.entity.type.ReasonType;
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
