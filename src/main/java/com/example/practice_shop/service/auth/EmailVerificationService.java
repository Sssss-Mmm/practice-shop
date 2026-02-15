package com.example.practice_shop.service.auth;

import com.example.practice_shop.constant.Status;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final Duration EMAIL_TOKEN_VALIDITY = Duration.ofHours(24);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendBaseUrl;

    /**
     * 이메일 인증 재전송
     * @param email
     */
    @Transactional
    public void resendEmailVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("이미 이메일 인증이 완료되었습니다.");
        }

        applyEmailVerificationWindow(user);
        sendVerificationEmail(user);
    }

    /**
     * 이메일 인증 처리
     * @param token
     */
    @Transactional
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

        user.completeEmailVerification(); // User 엔티티에 추가한 메서드 사용
    }

    /**
     * 이메일 인증 토큰과 만료 시간을 설정합니다.
     * @param user 사용자 엔티티
     */
    @Transactional
    public void applyEmailVerificationWindow(User user) {
        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
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
    public void sendVerificationEmail(User user) {
        String verificationLink = buildVerificationLink(user.getEmailVerificationToken());
        String body = String.format(
                "안녕하세요, %s님!\n\n아래 링크를 클릭하여 이메일 인증을 완료해 주세요:\n%s\n\n링크는 %d시간 동안 유효합니다.",
                user.getName(),
                verificationLink,
                EMAIL_TOKEN_VALIDITY.toHours()
        );
        emailService.sendEmail(
                user.getEmail(),
                "Practice Shop 이메일 인증",
                body
        );
    }

    private String buildVerificationLink(String token) {
        return String.format("%s/verify-email?token=%s", frontendBaseUrl, token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
