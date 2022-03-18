package com.example.BlindCafe.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Interest {

    @Id
    @GeneratedValue
    @Column(name = "interest_id")
    private Long id;

    @Column(length = 10, nullable = false)
    private String name;

    private Boolean isMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_interest_id")
    private Interest main;

    @OneToMany(mappedBy = "main")
    private List<Interest> child = new ArrayList<>();
}
