package com.example.practice_shop.config;

import com.example.practice_shop.security.JwtAuthenticationFilter;
import com.example.practice_shop.security.JwtTokenProvider;
import com.example.practice_shop.security.OAuth2AuthenticationSuccessHandler;
import com.example.practice_shop.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스입니다.
 * 웹 보안, CORS, OAuth2, JWT 관련 설정을 구성합니다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOauth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈을 등록합니다.
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security의 필터 체인을 설정합니다.
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF(Cross-Site Request Forgery) 보호를 비활성화합니다. (Stateless API 서버의 경우 보통 비활성화)
            .csrf(csrf -> csrf.disable())
            // 세션을 사용하지 않는 Stateless 서버로 설정합니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // HTTP 요청에 대한 인가 규칙을 설정합니다.
            .authorizeHttpRequests(auth -> auth
                // 지정된 경로들은 인증 없이 접근을 허용합니다.
                .requestMatchers("/", "/auth/**", "/oauth2/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // 그 외 모든 요청은 인증이 필요합니다.
                .anyRequest().authenticated()
            )
            // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가합니다.
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            // OAuth2 로그인을 활성화하고 관련 설정을 구성합니다.
            .oauth2Login(oauth2 -> oauth2
                // 사용자 정보를 가져오는 엔드포인트 및 사용자 서비스를 설정합니다.
                .userInfoEndpoint(userInfo -> userInfo.userService(customOauth2UserService))
                // OAuth2 인증 성공 시 실행될 핸들러를 설정합니다.
                .successHandler(oAuth2AuthenticationSuccessHandler)
            );

        return http.build();
    }
}
