package com.example.practice_shop.dtos.Product;

import com.example.practice_shop.constant.ProductStatus;
import com.example.practice_shop.entity.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String productName;
    private int price;
    private ProductStatus status;
    private String description;
    private String imageUrl;
    private String sellerName;
    private String sellerEmail;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .status(product.getStatus())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .sellerName(product.getSeller().getName())
                .sellerEmail(product.getSeller().getEmail())
                .build();
    }
}
