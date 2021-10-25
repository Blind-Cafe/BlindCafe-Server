package com.example.BlindCafe.type;

import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
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

    public static AgeRange getAgeRange(String range) {
        if (range.equals("20~29"))
            return R20;
        else if (range.equals("30~39"))
            return R30;
        else if (range.equals("40~49"))
            return R40;
        else if (range.equals("50~59"))
            return R50;
        else if (range.equals("60~69"))
            return R60;
        else if (range.equals("70~79"))
            return R70;
        else if (range.equals("80~89"))
            return R80;
        else if (range.equals("90~99"))
            return R90;
        else if (range.equals("100~109"))
            return R100;
        else if (range.equals("100~109"))
            return R110;
        else
            throw new BlindCafeException(CodeAndMessage.INVALID_REQUEST);
    }
}
