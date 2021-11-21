package com.example.BlindCafe.entity.topic;

import com.example.BlindCafe.entity.Interest;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@DiscriminatorValue("S")
@Getter
@Setter
public class Subject extends Topic {

    @Column(name = "interest_id")
    private Long interestId;
    private String subject;
}
