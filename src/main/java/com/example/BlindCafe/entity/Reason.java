package com.example.BlindCafe.entity;

import com.example.BlindCafe.type.ReasonType;
import lombok.*;

import javax.persistence.*;

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

    private ReasonType reasonType;
}
