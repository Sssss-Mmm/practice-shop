package com.example.practice_shop.service;

import com.example.practice_shop.dtos.Product.ProductRegistrationRequest;
import com.example.practice_shop.dtos.Product.ProductResponse;
import com.example.practice_shop.entity.Product;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.ProductRepository;
import com.example.practice_shop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.from(product);
    }

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

    @Transactional
    public void deleteProduct(Long productId, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!Objects.equals(product.getSeller().getEmail(), username)) {
            throw new RuntimeException("You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    private String saveImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        try {
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }
}
