package me.blindcafe.blindcafe.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Interest {

    @Id
    @GeneratedValue
    @Column(name = "interest_id")
    private Long id;

    @Column(length = 10, nullable = false)
    private String name;
}
