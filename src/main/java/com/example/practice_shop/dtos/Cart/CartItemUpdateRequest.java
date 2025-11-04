package com.example.practice_shop.dtos.Cart;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemUpdateRequest {
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private int quantity;
}
