package com.example.practice_shop.service;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.dtos.Auth.OAuth2RegistrationRequest;
import com.example.practice_shop.dtos.Auth.SignupRequest;
import com.example.practice_shop.dtos.Auth.UserLogin;
import com.example.practice_shop.dtos.Auth.UserLogout;
import com.example.practice_shop.dtos.User.UserProfileResponse;
import com.example.practice_shop.dtos.User.UserProfileUpdateRequest;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final EmailService emailService;

    private static final Duration EMAIL_TOKEN_VALIDITY = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_TOKEN_VALIDITY = Duration.ofHours(1);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendBaseUrl;

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
                .status(Status.INACTIVE)
                .build();

        userRepository.save(user);

        sendVerificationEmail(user);
    }
    /**
     * 이메일 인증 재전송
     * @param email
     */
    public void resendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("이미 이메일 인증이 완료되었습니다.");
        }

        applyEmailVerificationWindow(user);
        userRepository.save(user);
        sendVerificationEmail(user);
    }

    /**
     * 이메일 인증 처리
     * @param token
     */
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (user.isEmailVerified()) {
            return;
        }

        if (user.getEmailVerificationExpiredAt() != null
                && user.getEmailVerificationExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 토큰이 만료되었습니다. 이메일 인증을 다시 요청해 주세요.");
        }

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiredAt(null);
        user.setEmailVerificationSentAt(null);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    /**
     * 비밀번호 재설정 요청
     * @param email
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("이메일 인증을 완료한 이후에 비밀번호를 재설정할 수 있습니다.");
        }

        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiredAt(now.plus(PASSWORD_RESET_TOKEN_VALIDITY));
        userRepository.save(user);

        sendPasswordResetEmail(user);
    }

    /**
     * 비밀번호 재설정
     * @param token
     * @param newPassword
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 재설정 토큰입니다."));

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새로운 비밀번호를 입력해 주세요.");
        }

        if (user.getPasswordResetExpiredAt() == null
                || user.getPasswordResetExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("재설정 토큰이 만료되었습니다. 비밀번호 재설정을 다시 요청해 주세요.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiredAt(null);
        user.setStatus(Status.ACTIVE);
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

        if (!"local".equalsIgnoreCase(user.getProvider())) {
            throw new IllegalArgumentException("소셜 계정은 해당 플랫폼 로그인을 사용하세요.");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
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
        user.setLastLoginAt(java.time.LocalDateTime.now()); // 마지막 로그인 시각 업데이트
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

    /**
     * OAuth2 사용자의 추가 정보 등록을 처리하고 JWT 토큰을 발급합니다.
     * @param request OAuth2 등록 요청 정보 DTO
     * @return Access Token과 Refresh Token이 담긴 Map
     */
    public Map<String, String> completeOAuth2Registration(OAuth2RegistrationRequest request) {
        // 임시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(request.getTemporaryToken())) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다.");
        }

        // 임시 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmail(request.getTemporaryToken());

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 사용자 정보 업데이트
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNickname(request.getNickname());
        user.setRegion(request.getRegion());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setStatus(Status.ACTIVE);
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        userRepository.save(user);

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getName(), user.getRole());

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
     * 만료된 Access Token을 갱신하고 새로운 토큰을 발급합니다.
     * @param refreshToken 클라이언트로부터 받은 Refresh Token
     * @return 새로운 Access Token과 Refresh Token이 담긴 Map
     */
    public Map<String, String> refreshToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 이메일 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 해당 이메일의 사용자를 찾아 데이터베이스에 저장된 Refresh Token과 비교
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

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
        userRepository.save(user);

        // 토큰을 Map에 담아 반환
        Map<String, String> response = new HashMap<>();
                response.put("accessToken", newAccessToken);
                response.put("refreshToken", newRefreshToken);
        
                return response;
            }
        
            public UserProfileResponse getUserProfile(String email) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
                return UserProfileResponse.from(user);
            }
    
    /**
     * 업데이트된 사용자 프로필 정보를 저장합니다.
     * @param email
     * @param request
     * @return
     */
    public UserProfileResponse updateUserProfile(String email, UserProfileUpdateRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        // 사용자 정보 업데이트
        user.setNickname(request.getNickname());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRegion(request.getRegion());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        userRepository.save(user);

        return UserProfileResponse.from(user);
    }

    /**
     * 이메일 인증 토큰과 만료 시간을 설정합니다.
     * @param user 사용자 엔티티
     */
    private void applyEmailVerificationWindow(User user) {
        // 고유한 토큰 생성
        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
        // 토큰 및 만료 시간 설정
        user.setEmailVerificationToken(token);
        user.setEmailVerificationSentAt(now);
        user.setEmailVerificationExpiredAt(now.plus(EMAIL_TOKEN_VALIDITY));
        user.setEmailVerified(false);
        if (user.getStatus() == Status.ACTIVE) {
            user.setStatus(Status.INACTIVE);
        }
    }

    /**
     * 이메일 인증 메일을 전송합니다.
     * @param user 사용자 엔티티
     */
    private void sendVerificationEmail(User user) {
        // 이메일 본문 구성
        String verificationLink = buildVerificationLink(user.getEmailVerificationToken());
        String body = String.format(
                "안녕하세요, %s님!\n\n아래 링크를 클릭하여 이메일 인증을 완료해 주세요:\n%s\n\n링크는 %d시간 동안 유효합니다.",
                user.getName(),
                verificationLink,
                EMAIL_TOKEN_VALIDITY.toHours()
        );
        // 이메일 전송
        emailService.sendEmail(
                user.getEmail(),
                "Practice Shop 이메일 인증",
                body
        );
    }

    /**
     * 비밀번호 재설정 이메일을 전송합니다.
     * @param user 사용자 엔티티
     */
    private void sendPasswordResetEmail(User user) {
        // 이메일 본문 구성
        String resetLink = buildPasswordResetLink(user.getPasswordResetToken());
        // 이메일 본문 구성
        String body = String.format(
                "안녕하세요, %s님!\n\n아래 링크를 클릭하여 비밀번호를 재설정해 주세요:\n%s\n\n링크는 %d분 동안 유효합니다.",
                user.getName(),
                resetLink,
                PASSWORD_RESET_TOKEN_VALIDITY.toMinutes()
        );
        // 이메일 전송
        emailService.sendEmail(
                user.getEmail(),
                "Practice Shop 비밀번호 재설정",
                body
        );
    }

    /**
     * 이메일 인증 링크를 생성합니다.
     * @param token
     * @return
     */
    private String buildVerificationLink(String token) {
        return String.format("%s/verify-email?token=%s", frontendBaseUrl, token);
    }

    /**
     * 비밀번호 재설정 링크를 생성합니다.
     * @param token
     * @return
     */
    private String buildPasswordResetLink(String token) {
        return String.format("%s/reset-password?token=%s", frontendBaseUrl, token);
    }
    
    /**
     * 고유한 토큰을 생성합니다.
     * @return 생성된 토큰 문자열
     */
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
        
