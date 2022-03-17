package com.example.BlindCafe.domain.topic;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@DiscriminatorValue("S")
@Getter
@Setter
public class Subject extends Topic {

    @Column(name = "interest_id")
    private Long interestId;
    private String subject;
}
