package com.example.BlindCafe.entity.topic;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Topic {

    @Id
    @GeneratedValue
    @Column(name = "topic_id")
    private Long id;
}
