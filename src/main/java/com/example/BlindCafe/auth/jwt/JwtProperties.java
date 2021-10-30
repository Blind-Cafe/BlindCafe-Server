package com.example.BlindCafe.auth.jwt;

/**
 * Todo
 * 만료 시간 줄이기 (현재 180일)
 * refresh token
 */
public class JwtProperties {
    public static final long EXPIRATION_TIME = 6 * 30 * 24 * 60 * 60 * 1000L;
    public static final String HEADER_NAME = "X-ACCESS_TOKEN";
}
