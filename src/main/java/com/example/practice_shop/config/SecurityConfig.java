package com.example.practice_shop.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.practice_shop.security.JwtAuthenticationFilter;
import com.example.practice_shop.security.JwtTokenProvider;


@Configuration
public class SecurityConfig {
    /**
     * JWT 토큰 제공자
     */
    private final JwtTokenProvider jwtTokenProvider;

    /*
     * 생성자 주입
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    /**
     * 비밀번호 인코더 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * 보안 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*
         * 보안 설정
         * - CSRF 비활성화 (API 개발용)
         * - 모든 요청 허용
         * - JWT 인증 필터 추가
         * - 기본 로그인 폼 제거
         * - Basic Auth 제거
         */
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 개발용)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/","/auth/**").permitAll() 
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .formLogin(login -> login.disable()) // 기본 로그인 폼 제거
            .httpBasic(basic -> basic.disable()); // Basic Auth 제거

        return http.build();
    }
}
