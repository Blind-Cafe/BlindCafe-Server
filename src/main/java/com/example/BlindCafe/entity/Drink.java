package com.example.BlindCafe.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
public class Drink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drink_id")
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;
}
