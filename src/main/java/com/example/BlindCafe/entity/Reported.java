package com.example.BlindCafe.entity;

import lombok.*;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reported {

    @Id
    @GeneratedValue
    @Column(name = "reported_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int count;

    @OneToMany(mappedBy = "defendant", cascade = ALL)
    private List<Report> reports = new ArrayList<>();
}
