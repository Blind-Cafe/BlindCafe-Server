package com.example.BlindCafe.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static final Long HOUR_OF_ONE_DAY = 24L;
    public static final Long HOUR_OF_TWO_DAYS = 48L;

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static LocalDateTime fromString(String str) {
        return LocalDateTime.parse(str, formatter);
    }
}
