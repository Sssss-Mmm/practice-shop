package com.example.practice_shop.dtos.Product;

import com.example.practice_shop.constant.ProductStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRegistrationRequest {
    private String productName;
    private int price;
    private ProductStatus status;
    private String description;
}
