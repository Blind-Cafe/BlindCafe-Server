package com.example.BlindCafe.entity;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
public class Address {

    private String state;
    private String region;

}
