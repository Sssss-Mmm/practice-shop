package com.example.practice_shop.dtos.Order;

import com.example.practice_shop.constant.OrderStatus;
import com.example.practice_shop.constant.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String contactNumber;
    private String recipientName;
    private String postalCode;
    private String deliveryInstructions;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
