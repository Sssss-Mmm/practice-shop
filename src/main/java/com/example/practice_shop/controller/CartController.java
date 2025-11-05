package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Cart.CartItemRequest;
import com.example.practice_shop.dtos.Cart.CartItemUpdateRequest;
import com.example.practice_shop.dtos.Cart.CartResponse;
import com.example.practice_shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 조회
     * @param authentication
     * @return
     */
    @GetMapping
    @Operation(summary = "장바구니 조회", description = "현재 로그인한 사용자의 장바구니를 조회합니다.")
    public ResponseEntity<CartResponse> getCartItems(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(cartService.getCart(userEmail));
    }

    /**
     * 장바구니에 상품 추가
     * @param authentication
     * @param request
     * @return
     */
    @PostMapping
    @Operation(summary = "장바구니 추가", description = "상품을 장바구니에 추가합니다.")
    public ResponseEntity<CartResponse> addCartItem(Authentication authentication,
                                                    @Valid @RequestBody CartItemRequest request) {
        String userEmail = authentication.getName();
        cartService.addItem(userEmail, request);
        return ResponseEntity.ok(cartService.getCart(userEmail));
    }

    /**
     * 장바구니 수량 수정
     * @param authentication
     * @param cartItemId
     * @param request
     * @return
     */
    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 수량 수정", description = "장바구니 항목의 수량을 수정합니다.")
    public ResponseEntity<CartResponse> updateCartItem(Authentication authentication,
                                                       @PathVariable Long cartItemId,
                                                       @Valid @RequestBody CartItemUpdateRequest request) {
        String userEmail = authentication.getName();
        cartService.updateItemQuantity(userEmail, cartItemId, request);
        return ResponseEntity.ok(cartService.getCart(userEmail));
    }

    /**
     * 장바구니 항목 삭제
     * @param authentication
     * @param cartItemId
     * @return
     */
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 특정 항목을 삭제합니다.")
    public ResponseEntity<CartResponse> removeCartItem(Authentication authentication,
                                                       @PathVariable Long cartItemId) {
        String userEmail = authentication.getName();
        cartService.removeItem(userEmail, cartItemId);
        return ResponseEntity.ok(cartService.getCart(userEmail));
    }
    
    /**
     * 장바구니 비우기
     * @param authentication
     * @return
     */
    @DeleteMapping
    @Operation(summary = "장바구니 비우기", description = "장바구니를 비웁니다.")
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        String userEmail = authentication.getName();
        cartService.clearCart(userEmail);
        return ResponseEntity.ok(cartService.getCart(userEmail));
    }
}
