package com.example.practice_shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice_shop.dtos.Cart.CartItemRequest;
import com.example.practice_shop.dtos.Cart.CartItemResponse;
import com.example.practice_shop.dtos.Cart.CartItemUpdateRequest;
import com.example.practice_shop.dtos.Cart.CartResponse;
import com.example.practice_shop.entity.Cart;
import com.example.practice_shop.entity.CartItem;
import com.example.practice_shop.entity.Product;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.CartItemRepository;
import com.example.practice_shop.repository.CartRepository;
import com.example.practice_shop.repository.ProductRepository;
import com.example.practice_shop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> initializeCart(user));

        List<CartItemResponse> itemResponses = new ArrayList<>();
        long totalPrice = 0;
        int totalItems = 0;

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                CartItemResponse response = toResponse(item);
                itemResponses.add(response);
                totalItems += response.getQuantity();
                totalPrice += response.getSubtotal();
            }
        }

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public CartItemResponse addItem(String email, CartItemRequest request) {
        User user = getUserByEmail(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> initializeCart(user));

        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, product);
        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            cartItem.setCount(cartItem.getCount() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .count(request.getQuantity())
                    .build();
            cart.getCartItems().add(cartItem);
        }
        CartItem saved = cartItemRepository.save(cartItem);
        return toResponse(saved);
    }

    @Transactional
    public CartItemResponse updateItemQuantity(String email, Long cartItemId, CartItemUpdateRequest request) {
        CartItem cartItem = getCartItemForUser(email, cartItemId);
        cartItem.setCount(request.getQuantity());
        cartItemRepository.save(cartItem);
        return toResponse(cartItem);
    }

    @Transactional
    public void removeItem(String email, Long cartItemId) {
        CartItem cartItem = getCartItemForUser(email, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        cartRepository.findByUser(user).ifPresent(cart -> {
            if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                cartItemRepository.deleteAll(cart.getCartItems());
                cart.getCartItems().clear();
                cartRepository.save(cart);
            }
        });
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Cart initializeCart(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .cartItems(new ArrayList<>())
                .build();
        user.setCart(cart);
        return cartRepository.save(cart);
    }

    private CartItem getCartItemForUser(String email, Long cartItemId) {
        User user = getUserByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (cartItem.getCart() == null || cartItem.getCart().getUser() == null
                || !cartItem.getCart().getUser().getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("해당 장바구니 항목에 대한 권한이 없습니다.");
        }

        return cartItem;
    }

    private CartItemResponse toResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        int subtotal = product.getPrice() * cartItem.getCount();
        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .quantity(cartItem.getCount())
                .subtotal(subtotal)
                .build();
    }
}
