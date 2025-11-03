package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Product.ProductRegistrationRequest;
import com.example.practice_shop.dtos.Product.ProductResponse;
import com.example.practice_shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "모든 상품 목록을 조회합니다.")
    public ResponseEntity<List<ProductResponse>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    public ResponseEntity<?> registerProduct(
            @RequestPart("request") ProductRegistrationRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) {
        productService.registerProduct(request, imageFile, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestPart("request") ProductRegistrationRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) {
        productService.updateProduct(productId, request, imageFile, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long productId,
            Authentication authentication) {
        productService.deleteProduct(productId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
