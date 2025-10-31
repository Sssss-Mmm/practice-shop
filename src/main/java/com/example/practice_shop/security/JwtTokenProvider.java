package com.example.practice_shop.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT(JSON Web Token)를 생성하고 검증하는 역할을 하는 클래스입니다.
 */
@Component
public class JwtTokenProvider {

    private final Key key; // JWT 서명에 사용할 키
    private final long accessTokenValidity; // Access Token의 유효 시간
    private final long refreshTokenValidity; // Refresh Token의 유효 시간
    private final long temporaryRegistrationTokenValidity; // 임시 등록 토큰의 유효 시간

    /**
     * 생성자. application.yml에서 jwt.secret 값을 주입받아 초기화합니다.
     * @param jwtSecret JWT 서명에 사용할 비밀 키 문자열
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = 1000L * 60 * 60; // 1시간
        this.refreshTokenValidity = 1000L * 60 * 60 * 24 * 7; // 7일
        this.temporaryRegistrationTokenValidity = 1000L * 60 * 5; // 5분
    }

    /**
     * Access Token을 생성합니다.
     * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(String subject, String username) {
        return createToken(subject, username, accessTokenValidity);
    }

    /**
     * Refresh Token을 생성합니다.
     * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(String subject, String username) {
        return createToken(subject, username, refreshTokenValidity);
    }

    /**
     * 임시 등록 토큰을 생성합니다. (OAuth2 추가 정보 입력용)
     * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
     * @return 생성된 임시 등록 토큰 문자열
     */
    public String createTemporaryRegistrationToken(String subject, String username) {
        return createToken(subject, username, temporaryRegistrationTokenValidity);
    }

    /**
     * 주어진 유효 시간을 바탕으로 JWT를 생성하는 내부 메소드입니다.
     * @param subject 토큰의 주체
     * @param validity 토큰의 유효 시간 (밀리초)
     * @return 생성된 JWT 문자열
     */
    private String createToken(String subject, String username, long validity) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(subject) // 토큰 주체 설정 (이메일)
                .claim("username", username) // 사용자 이름 클레임 추가
                .setIssuedAt(now) // 토큰 발급 시간 설정
                .setExpiration(expiry) // 토큰 만료 시간 설정
                .signWith(key) // 서명 키 설정
                .compact(); // JWT 생성
    }

    /**
     * 주어진 토큰의 유효성을 검증합니다.
     * @param token 검증할 JWT 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key) // 서명 키로 검증
                .build()
                .parseClaimsJws(token); // 토큰 파싱 및 검증
            return true;
        } catch (Exception e) {
            // 파싱 또는 검증 중 예외가 발생하면 유효하지 않은 토큰으로 간주
            return false;
        }
    }

    /**
     * 토큰에서 사용자의 이메일 정보를 추출합니다.
     * @param token 정보를 추출할 JWT 문자열
     * @return 추출된 사용자 이메일
     */
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // subject 클레임(이메일) 반환
    }

    /**
     * 토큰에서 사용자의 이름 정보를 추출합니다.
     * @param token 정보를 추출할 JWT 문자열
     * @return 추출된 사용자 이름
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class); // username 클레임 반환
    }
}