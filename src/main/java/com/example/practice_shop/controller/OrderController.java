package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Order.OrderCreateRequest;
import com.example.practice_shop.dtos.Order.OrderResponse;
import com.example.practice_shop.dtos.common.PagedResponse;
import com.example.practice_shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "장바구니 상품을 기반으로 주문을 생성합니다.")
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication,
                                                     @Valid @RequestBody OrderCreateRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.createOrder(email, request));
    }

    @GetMapping
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 내역을 페이지 단위로 조회합니다.")
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(Authentication authentication,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getOrders(email, page, size));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다.")
    public ResponseEntity<OrderResponse> getOrder(Authentication authentication,
                                                  @PathVariable Long orderId) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getOrderDetail(email, orderId));
    }
}
