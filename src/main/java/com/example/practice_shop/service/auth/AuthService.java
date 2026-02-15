package com.example.practice_shop.service.auth;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.dtos.Auth.UserLogout;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.exception.CustomException;
import com.example.practice_shop.exception.ErrorCode;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로컬 사용자의 로그인을 처리하고 JWT 토큰을 발급합니다.
     * @param userLogin 로그인 요청 정보 DTO
     * @return Access Token과 Refresh Token이 담긴 Map
     */
    @Transactional
    public Map<String, String> localLogin(UserLogin userLogin) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userLogin.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!"local".equalsIgnoreCase(user.getProvider())) {
            throw new IllegalArgumentException("소셜 계정은 해당 플랫폼 로그인을 사용하세요.");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getName(), user.getRole());

        // Refresh Token을 데이터베이스에 저장
        user.setRefreshToken(refreshToken);
        user.updateLastLoginAt();
        // user.setLastLoginAt(java.time.LocalDateTime.now()); // Entity 메서드 사용

        // 토큰을 Map에 담아 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }

    /**
     * 사용자의 로그아웃을 처리합니다.
     * 데이터베이스에서 Refresh Token을 제거합니다.
     * @param userLogout 로그아웃 요청 정보 DTO
     */
    @Transactional
    public void logout(UserLogout userLogout) {
        String refreshToken = userLogout.getRefreshToken();

        // Access Token과 Refresh Token의 유효성 검증
        if (!jwtTokenProvider.validateToken(userLogout.getAccessToken())) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }
        if (refreshToken == null || !jwtTokenProvider.validateToken(userLogout.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        // Refresh Token에서 이메일 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 해당 이메일의 사용자를 찾아 Refresh Token을 null로 설정
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setRefreshToken(null);
        // JPA Dirty Checking
    }

    /**
     * 만료된 Access Token을 갱신하고 새로운 토큰을 발급합니다.
     * @param refreshToken 클라이언트로부터 받은 Refresh Token
     * @return 새로운 Access Token과 Refresh Token이 담긴 Map
     */
    @Transactional
    public Map<String, String> refreshToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 이메일 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 해당 이메일의 사용자를 찾아 데이터베이스에 저장된 Refresh Token과 비교
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        if (!user.isEmailVerified() || user.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getName(), user.getRole());

        // 새로운 Refresh Token을 데이터베이스에 저장
        user.setRefreshToken(newRefreshToken);
        // JPA Dirty Checking

        // 토큰을 Map에 담아 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);

        return response;
    }
}
