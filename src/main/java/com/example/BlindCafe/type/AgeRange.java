package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum AgeRange {

    R20("20~29"),
    R30("30~39"),
    R40("40~49"),
    R50("50~59"),
    R60("60~69"),
    R70("70~79"),
    R80("80~89"),
    R90("90~99"),
    R100("100~109"),
    R110("110~119");

    private final String description;
}
