package com.example.practice_shop.dtos.Order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private int price;
    private int quantity;
    private int lineTotal;
    private String imageUrl;
}
