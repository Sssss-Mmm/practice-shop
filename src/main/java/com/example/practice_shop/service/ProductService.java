package com.example.practice_shop.service;

import com.example.practice_shop.dtos.Product.ProductRegistrationRequest;
import com.example.practice_shop.dtos.Product.ProductResponse;
import com.example.practice_shop.dtos.common.PagedResponse;
import com.example.practice_shop.entity.Product;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.ProductRepository;
import com.example.practice_shop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final String UPLOAD_DIR = "./uploads/";

    /**
     * 상품 목록 조회
     * @param page
     * @param size
     * @return
     */
    public PagedResponse<ProductResponse> getProducts(int page, int size) {
        int validatedPage = Math.max(page, 0);
        int validatedSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Product> productPage = productRepository.findAll(pageRequest);

        List<ProductResponse> content = productPage.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }
    /**
     * 상품 상세 조회
     * @param productId
     * @return
     */
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.from(product);
    }

    /**
     * 상품 등록
     * @param request
     * @param imageFile
     * @param username
     */
    @Transactional
    public void registerProduct(ProductRegistrationRequest request, MultipartFile imageFile, String username) {
        User seller = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = saveImage(imageFile);

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setSeller(seller);
        product.setStatus(request.getStatus());
        product.setDescription(request.getDescription());
        product.setImageUrl(imageUrl);

        productRepository.save(product);
    }

    /**
     * 상품 수정
     * @param productId
     * @param request
     * @param imageFile
     * @param username
     */
    @Transactional
    public void updateProduct(Long productId, ProductRegistrationRequest request, MultipartFile imageFile, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!Objects.equals(product.getSeller().getEmail(), username)) {
            throw new RuntimeException("You are not authorized to update this product");
        }

        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());
        product.setDescription(request.getDescription());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setImageUrl(imageUrl);
        }

        productRepository.save(product);
    }

    /**
     * 상품 삭제
     * @param productId
     * @param username
     */
    @Transactional
    public void deleteProduct(Long productId, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!Objects.equals(product.getSeller().getEmail(), username)) {
            throw new RuntimeException("You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    /**
     * 이미지 저장
     * @param imageFile
     * @return
     */
    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        try {
            String originalFilename = imageFile.getOriginalFilename();
            String safeFilename = normalizeFilename(originalFilename);
            String fileName = UUID.randomUUID() + "_" + safeFilename;
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

    /**
     * 파일명 정규화
     * @param originalFilename
     * @return
     */
    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "image";
        }

        String trimmed = originalFilename.trim();
        String extension = "";
        int lastDot = trimmed.lastIndexOf('.');
        if (lastDot != -1 && lastDot < trimmed.length() - 1) {
            extension = trimmed.substring(lastDot);
            trimmed = trimmed.substring(0, lastDot);
        }

        String sanitizedBase = trimmed.replaceAll("[^a-zA-Z0-9._-]", "_");
        sanitizedBase = sanitizedBase.replaceAll("_+", "_");
        sanitizedBase = sanitizedBase.replaceAll("^_|_$", "");

        if (sanitizedBase.isBlank()) {
            sanitizedBase = "image";
        }

        return sanitizedBase + extension.toLowerCase();
    }
}
