package com.example.practice_shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.practice_shop.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /** 이메일로 사용자 조회 */
    public Optional<User> findByEmail(String email);
    /** 이메일 인증 토큰으로 사용자 조회 */
    public Optional<User> findByEmailVerificationToken(String token);
    /** 비밀번호 재설정 토큰으로 사용자 조회 */
    public Optional<User> findByPasswordResetToken(String token);
}
