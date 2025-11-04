package com.example.practice_shop.dtos.Cart;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private int price;
    private String imageUrl;
    private int quantity;
    private int subtotal;
}
