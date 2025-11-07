package com.example.practice_shop.repository;

import com.example.practice_shop.entity.Order;
import com.example.practice_shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    /** 사용자별 주문 내역 페이징 조회 */
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /** 사용자별 주문 상세 조회 */
    Optional<Order> findByIdAndUser(Long id, User user);
}
