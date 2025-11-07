package com.example.practice_shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practice_shop.entity.Cart;
import com.example.practice_shop.entity.CartItem;
import com.example.practice_shop.entity.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    /** 장바구니와 상품으로 장바구니 항목 조회 */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
