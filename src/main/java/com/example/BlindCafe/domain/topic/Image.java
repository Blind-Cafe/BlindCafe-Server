package com.example.BlindCafe.domain.topic;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("I")
@Getter
@Setter
public class Image extends Topic {

    private String title;
    private String src;
}
