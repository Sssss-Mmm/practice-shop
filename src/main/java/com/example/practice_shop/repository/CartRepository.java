package com.example.practice_shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practice_shop.entity.Cart;
import com.example.practice_shop.entity.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
    /** 사용자별 장바구니 조회 */
    Optional<Cart> findByUser(User user);
}
