package com.example.practice_shop.service;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.dtos.Auth.UserLogout;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * (회원가입, 로컬 로그인, 로그아웃 등)
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로컬 사용자의 회원가입을 처리합니다.
     * @param signupRequest 회원가입 요청 정보 DTO
     */
    public void register(SignupRequest signupRequest) {
        // 이메일 중복 체크
        userRepository.findByEmail(signupRequest.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("Email already in use");
        });

        // 사용자 정보 빌드
        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword() != null ? passwordEncoder.encode(signupRequest.getPassword()) : null)
                .name(signupRequest.getName())
                .phoneNumber(signupRequest.getPhoneNumber())
                .nickname(signupRequest.getNickname())
                .region(signupRequest.getRegion())
                .address(signupRequest.getAddress())
                .gender(signupRequest.getGender())
                .birthDate(signupRequest.getBirthDate())
                .role(signupRequest.getRole())
                .provider(signupRequest.getProvider())
                .providerId(signupRequest.getProviderId() != null ? signupRequest.getProviderId() : "local_" + UUID.randomUUID())
                .status(Status.ACTIVE)
                .build();

        userRepository.save(user);
    }

    /**
     * 로컬 사용자의 로그인을 처리하고 JWT 토큰을 발급합니다.
     * @param userLogin 로그인 요청 정보 DTO
     * @return Access Token과 Refresh Token이 담긴 Map
     */
    public Map<String, String> localLogin(UserLogin userLogin) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userLogin.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치 하지 않습니다.");
        }

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // Refresh Token을 데이터베이스에 저장
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

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
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        user.setRefreshToken(null);
        userRepository.save(user);
    }
}
