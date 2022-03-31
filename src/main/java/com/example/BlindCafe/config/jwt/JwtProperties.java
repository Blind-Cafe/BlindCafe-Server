package com.example.BlindCafe.config.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

    public static String key1;
    public static String key2;
    public static String key3;

    @Value("${secret.key1}")
    public void setKey1(String value) {
        key1 = value;
    }

    @Value("${secret.key2}")
    public void setKey2(String value) {
        key2 = value;
    }

    @Value("${secret.key3}")
    public void setKey3(String value) {
        key3 = value;
    }

    public static final String HEADER_NAME = "AUTHORIZATION";
    public static final long ACCESS_EXPIRED_TIME = 14 * 24 * 60 * 60 * 1000L;
    public static final long REFRESH_EXPIRED_TIME = 6 * 30 * 24 * 60 * 60 * 1000L;

    public static final String REFRESH_TOKEN_PREFIX = "USER-REFRESH-TOKEN:";
}
