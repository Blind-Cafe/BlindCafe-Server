package com.example.BlindCafe.entity;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long id;

    @Column(length = 10, nullable = false)
    private String name;

    private Boolean isParent;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Interest parent;

    @OneToMany(mappedBy = "parent")
    private List<Interest> child = new ArrayList<>();
}
