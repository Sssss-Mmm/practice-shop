package com.example.practice_shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    /** JavaMailSender 빈 주입 */
    private final JavaMailSender mailSender;

    /** 발신자 주소 설정 */
    @Value("${mail.from-address:${spring.mail.username:}}")
    private String fromAddress;

    /**
     * 이메일을 전송합니다.
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param body 이메일 본문
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(fromAddress)) {
                message.setFrom(fromAddress);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            
        } catch (Exception ex) {
            log.error("Failed to send email to {}", to, ex);
            throw new IllegalStateException("이메일 전송에 실패했습니다.", ex);
        }
    }
}
