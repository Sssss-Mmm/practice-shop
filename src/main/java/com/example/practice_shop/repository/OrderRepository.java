package com.example.practice_shop.repository;

import com.example.practice_shop.entity.Order;
import com.example.practice_shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Order> findByIdAndUser(Long id, User user);
}
