package com.example.BlindCafe.entity;

import lombok.Getter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
public class Topic {

    @Id
    @GeneratedValue
    @Column(name = "topic_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "interest_id")
    private Interest interest;

    @Column(length = 50, nullable = false)
    private String subject;
}
