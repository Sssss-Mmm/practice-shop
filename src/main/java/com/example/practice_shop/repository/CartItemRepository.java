package com.example.practice_shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practice_shop.entity.Cart;
import com.example.practice_shop.entity.CartItem;
import com.example.practice_shop.entity.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
