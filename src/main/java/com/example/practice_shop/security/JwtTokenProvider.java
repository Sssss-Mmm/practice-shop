package com.example.practice_shop.security;

import com.example.practice_shop.constant.Role;
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
    private static final int MIN_SECRET_KEY_BYTES = 32; // HMAC-SHA256 최소 256비트

    /**
     * 생성자. application.yml에서 jwt 설정값을 주입받아 초기화합니다.
     * @param jwtSecret JWT 서명에 사용할 비밀 키 문자열 (최소 32자 이상)
     * @param accessTokenValidity Access Token 유효시간 (밀리초, 기본값 1시간)
     * @param refreshTokenValidity Refresh Token 유효시간 (밀리초, 기본값 7일)
     * @param tempTokenValidity 임시 토큰 유효시간 (밀리초, 기본값 5분)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-validity:3600000}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity:604800000}") long refreshTokenValidity,
            @Value("${jwt.temp-token-validity:300000}") long tempTokenValidity) {
        
        // Secret Key 길이 검증 (HMAC-SHA256은 최소 256비트 필요)
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_KEY_BYTES) {
            throw new IllegalArgumentException(
                String.format("JWT secret key must be at least %d bytes (256 bits) for HMAC-SHA256. Current: %d bytes", 
                    MIN_SECRET_KEY_BYTES, keyBytes.length));
        }
        
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.temporaryRegistrationTokenValidity = tempTokenValidity;
    }

    /**
     * Access Token을 생성합니다.
     * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(String subject, String username, Role role) {
        return createToken(subject, username, role, accessTokenValidity);
    }

    /**
     * Refresh Token을 생성합니다.
     * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(String subject, String username, Role role) {
        return createToken(subject, username, role, refreshTokenValidity);
    }

    /**
     * 임시 등록 토큰을 생성합니다. (OAuth2 추가 정보 입력용)
     * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
     * @return 생성된 임시 등록 토큰 문자열
     */
    public String createTemporaryRegistrationToken(String subject, String username, Role role) {
        return createToken(subject, username, role, temporaryRegistrationTokenValidity);
    }

    /**
     * 주어진 유효 시간을 바탕으로 JWT를 생성하는 내부 메소드입니다.
     * @param subject 토큰의 주체
     * @param validity 토큰의 유효 시간 (밀리초)
     * @return 생성된 JWT 문자열
     */
    private String createToken(String subject, String username, Role role, long validity) {
        // 현재 시간과 유효 시간 계산
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        // JWT 생성
        return Jwts.builder()
                .setSubject(subject) // 토큰 주체 설정 (이메일)
                .claim("username", username) // 사용자 이름 클레임 추가
                .claim("role", role) // 사용자 역할 클레임 추가
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
            // 토큰 검증
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

    /**
     * 토큰에서 사용자의 역할 정보를 추출합니다.
     * @param token 정보를 추출할 JWT 문자열
     * @return 추출된 사용자 역할
     */
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class); // role 클레임 반환
    }
}