package com.example.BlindCafe.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
public class Drink {

    @Id
    @GeneratedValue
    @Column(name = "drink_id")
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;
}
