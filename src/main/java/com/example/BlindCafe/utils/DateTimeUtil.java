package com.example.BlindCafe.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static LocalDateTime fromString(String str) {
        return LocalDateTime.parse(str, formatter);
    }
}
