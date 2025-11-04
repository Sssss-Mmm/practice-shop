package com.example.practice_shop.dtos.Cart;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private int totalItems;
    private long totalPrice;
}
