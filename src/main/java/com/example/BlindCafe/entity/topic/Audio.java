package com.example.BlindCafe.entity.topic;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A")
@Getter
@Setter
public class Audio extends Topic {

    private String title;
    private String src;
}
