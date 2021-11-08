package com.example.BlindCafe.entity;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String state;
    private String region;

    @Override
    public String toString() {
        if (state != null && region != null)
            return state + " " + region;
        else
            return null;
    }
}
