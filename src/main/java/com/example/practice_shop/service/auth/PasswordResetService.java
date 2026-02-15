package com.example.practice_shop.service.auth;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final Duration PASSWORD_RESET_TOKEN_VALIDITY = Duration.ofHours(1);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendBaseUrl;

    /**
     * 비밀번호 재설정 요청
     * 보안: 계정 열거 공격 방지를 위해 이메일 존재 여부와 관계없이 동일한 응답 반환
     * @param email
     */
    @Transactional
    public void requestPasswordReset(String email) {
        // 이메일 존재 여부와 관계없이 항상 성공 응답 (계정 열거 공격 방지)
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isEmailVerified()) {
                String token = generateToken();
                user.initiatePasswordReset(token, PASSWORD_RESET_TOKEN_VALIDITY);
                // JPA Dirty Checking으로 저장됨
                sendPasswordResetEmail(user);
            }
        });
    }

    /**
     * 비밀번호 재설정
     * @param token
     * @param newPassword
     */
    @Transactional
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
        // JPA Dirty Checking
    }

    private void sendPasswordResetEmail(User user) {
        String resetLink = buildPasswordResetLink(user.getPasswordResetToken());
        String body = String.format(
                "안녕하세요, %s님!\n\n아래 링크를 클릭하여 비밀번호를 재설정해 주세요:\n%s\n\n링크는 %d분 동안 유효합니다.",
                user.getName(),
                resetLink,
                PASSWORD_RESET_TOKEN_VALIDITY.toMinutes()
        );
        emailService.sendEmail(
                user.getEmail(),
                "Practice Shop 비밀번호 재설정",
                body
        );
    }

    private String buildPasswordResetLink(String token) {
        return String.format("%s/reset-password?token=%s", frontendBaseUrl, token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
