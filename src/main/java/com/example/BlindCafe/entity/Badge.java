package com.example.BlindCafe.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class Badge {

    @Id
    @GeneratedValue
    @Column(name = "badge_id")
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;
}
