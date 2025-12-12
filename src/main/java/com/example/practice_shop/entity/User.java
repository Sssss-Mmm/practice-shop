package com.example.practice_shop.entity;

import java.time.LocalDateTime;


import com.example.practice_shop.constant.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.practice_shop.constant.*;


/*
 * 사용자 정보 엔티티
 */
@Entity // JPA 엔티티 선언
@Table(name = "users") // 테이블 이름 지정
@Getter // Getter 자동 생성
@Setter // Setter 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
@Builder // 빌더 패턴 지원
public class User extends BaseTimeEntity {

    /** 사용자 고유 아이디 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /** 사용자 이메일 */
    @Column(unique = true, nullable = false)
    private String email;

    /** 암호화된 비밀번호 */
    @Column(nullable = true)
    private String password;

    /** 사용자 이름 */
    @Column(name = "user_name", nullable = false)
    private String name;
    
    /** 닉네임 */
    @Column(unique = true, nullable = true)
    private String nickname;
    
    /** OAuth2 제공자 이름 */
    @Builder.Default
    @Column(nullable = true)
    private String provider = "local";

    /** OAuth2 제공자 고유 ID */
    @Column(name = "provider_id", unique = true, nullable = true)
    private String providerId;
    
    /** 역할(회원, 관리자) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** JWT Refresh Token 저장용 */
    @Column(length = 500)
    private String refreshToken;

    
    /** 거주 지역 */
    @Column(nullable = true)
    private String region;
    
    /** 상세 주소 */
    @Column(nullable = true)
    private String address;
    
    /** 핸드폰 번호 */
    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;
    
    /** 성별 */
    @Column(nullable = true)
    private String gender;
    
    /** 생년월일 */
    @Column(name = "birth_date", nullable = true)
    private String birthDate;

    /** 프로필 이미지 URL */
    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    /** 사용자 등급 */
    @Column(name = "user_grade", nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserGrade userGrade = UserGrade.BASIC; // 기본값 BASIC

    /** 누적 결제 금액 */
    @Column(name = "total_spend", nullable = false)
    @Builder.Default
    private Long totalSpend = 0L;

    /** 상태 */
    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE; // 기본값 ACTIVE

    /** 포인트 */
    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 0; // 기본값 0

    /** 이메일 인증 여부 */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /** 이메일 인증 토큰 */
    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;

    /** 이메일 인증 토큰 발송 시각 */
    @Column(name = "email_verification_sent_at")
    private LocalDateTime emailVerificationSentAt;

    /** 이메일 인증 토큰 만료 시각 */
    @Column(name = "email_verification_expired_at")
    private LocalDateTime emailVerificationExpiredAt;

    /** 이메일 인증 완료 시각 */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /** 비밀번호 재설정 토큰 */
    @Column(name = "password_reset_token", length = 64)
    private String passwordResetToken;

    /** 비밀번호 재설정 토큰 만료 시각 */
    @Column(name = "password_reset_expired_at")
    private LocalDateTime passwordResetExpiredAt;

    /** 마지막 로그인 시각 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;


    
    /** 마지막 로그인 시각 */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void accumulateSpend(long amount) {
        if (amount <= 0) {
            return;
        }
        if (this.totalSpend == null) {
            this.totalSpend = 0L;
        }
        this.totalSpend += amount;
        this.userGrade = UserGrade.fromTotalSpend(this.totalSpend);
    }
    
}
