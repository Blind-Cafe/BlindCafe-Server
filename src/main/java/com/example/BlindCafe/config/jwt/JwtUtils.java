package com.example.BlindCafe.config.jwt;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.exception.BlindCafeException;
import io.jsonwebtoken.*;
import org.springframework.data.util.Pair;

import java.security.Key;
import java.util.Date;

import static com.example.BlindCafe.config.jwt.JwtProperties.AUTHORIZATION_TYPE;
import static com.example.BlindCafe.exception.CodeAndMessage.*;

public class JwtUtils {

    public static String createAccessToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getId().toString());
        Date now = new Date();
        Pair<String, Key> key = JwtKey.getRandomKey();
        // Token 생성
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + JwtProperties.ACCESS_EXPIRED_TIME)) // 토큰 만료 시간 설정
                .setHeaderParam(JwsHeader.KEY_ID, key.getFirst()) // kid
                .signWith(key.getSecond()) // signature
                .compact();
    }

    public static String createWebAccessToken(Long userId) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        Date now = new Date();
        Pair<String, Key> key = JwtKey.getRandomKey();
        // Token 생성
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + JwtProperties.WEB_ACCESS_EXPIRED_TIME)) // 토큰 만료 시간 설정
                .setHeaderParam(JwsHeader.KEY_ID, key.getFirst()) // kid
                .signWith(key.getSecond()) // signature
                .compact();
    }

    public static String createRefreshToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getId().toString());
        Date now = new Date();
        Pair<String, Key> key = JwtKey.getRandomKey();
        // Token 생성
        return Jwts.builder()
                .setClaims(claims) // 정보 저장
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + JwtProperties.REFRESH_EXPIRED_TIME)) // 토큰 만료 시간 설정
                .setHeaderParam(JwsHeader.KEY_ID, key.getFirst()) // kid
                .signWith(key.getSecond()) // signature
                .compact();
    }

    public static String getUsedId(String token) {
        String[] split = token.split(AUTHORIZATION_TYPE);
        if (split.length < 2)
            throw new BlindCafeException(FAILED_AUTHORIZATION);
        token = split[1];
        try {
            return Jwts.parserBuilder()
                    .setSigningKeyResolver(SigningKeyResolver.instance)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        }  catch (ExpiredJwtException e) {
            throw new BlindCafeException(EXPIRED_TOKEN);
        } catch (IllegalArgumentException | MalformedJwtException | UnsupportedJwtException e) {
            throw new BlindCafeException(FAILED_AUTHORIZATION);
        }
    }
}
