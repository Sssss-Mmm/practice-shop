package com.example.practice_shop.entity;

import java.time.LocalDateTime;
import java.util.List;

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

    /** 상태 */
    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE; // 기본값 ACTIVE

    /** 마지막 로그인 시각 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "user")
    private List<Review> reviews;
    
    /** 마지막 로그인 시각 */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
