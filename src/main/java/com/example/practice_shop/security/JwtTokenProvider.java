package com.example.practice_shop.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.*;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    /**
     * JWT 서명에 사용할 비밀 키 생성
     */
    ;
    private Key key;

    /**
     * 액세스 토큰 유효 기간 (밀리초 단위)
     */
    private final long accessTokenValidity = 1000L * 60 * 60; // 60분

    public JwtTokenProvider(@Value("${JWT_SECRET}")String jwtSecret) {
        // 비밀 키를 바이트 배열로 변환하여 HMAC-SHA 키 생성
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 액세스 토큰 생성
     */
    public String createAccessToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);

        /* JWT 생성 및 서명
         * - subject: 토큰의 주체 (예: 사용자 ID)
         * - issuedAt: 토큰 발급 시간
         * - expiration: 토큰 만료 시간
         * - signWith: 서명에 사용할 키
         * - compact(): JWT 문자열 생성
         */
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 검증
     * @return 유효한 토큰이면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {

            /*
             * JWT 파싱 및 검증
             * - Jwts.parserBuilder(): JWT 파서 빌더 생성
             * - setSigningKey: 서명 검증에 사용할 키 설정
             * - build(): 파서 빌드
             * - parseClaimsJws: JWT 파싱 및 서명 검증
             * - 예외가 발생하지 않으면 토큰이 유효함
             */
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 email 추출
     */
    public String getEmail(String token) {
        /*
         * JWT 파싱 및 클레임 추출
         * - Jwts.parserBuilder(): JWT 파서 빌더 생성
         * - setSigningKey: 서명 검증에 사용할 키 설정
         * - build(): 파서 빌드
         * - parseClaimsJws: JWT 파싱 및 서명 검증
         * - getBody(): JWT 본문(클레임) 추출
         * - getSubject(): subject 클레임(이메일) 추출
         */
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}