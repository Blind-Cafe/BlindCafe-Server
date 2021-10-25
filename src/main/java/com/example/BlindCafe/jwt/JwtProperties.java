package com.example.BlindCafe.jwt;

/**
 * Todo
 * 만료 시간 10분으로 줄이기 (현재 30일)
 */
public class JwtProperties {
    public static final int EXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000;
    public static final String COOKIE_NAME = "JWT_AUTHENTICATION";
}
