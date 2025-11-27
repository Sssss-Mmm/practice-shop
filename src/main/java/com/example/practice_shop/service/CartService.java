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

    /**
     * 장바구니 조회
     * @param email
     * @return
     */
    @Transactional
    public CartResponse getCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> initializeCart(user));

        List<CartItemResponse> itemResponses = new ArrayList<>();
        long totalPrice = 0;
        int totalItems = 0;
        // 장바구니 항목 변환 및 총합 계산
        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                CartItemResponse response = toResponse(item);
                itemResponses.add(response);
                totalItems += response.getQuantity();
                totalPrice += response.getSubtotal();
            }
        }
        // CartResponse 생성
        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }

    /**
     * 장바구니에 상품 추가
     * @param email
     * @param request
     * @return
     */
    @Transactional
    public CartItemResponse addItem(String email, CartItemRequest request) {
        User user = getUserByEmail(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> initializeCart(user));
        // 장바구니 항목 리스트 초기화
        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        }
        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 기존 장바구니 항목 확인
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, product);
        CartItem cartItem;
        // 기존 항목이 있으면 수량 증가, 없으면 새 항목 추가
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
        // 장바구니 항목 저장
        CartItem saved = cartItemRepository.save(cartItem);
        return toResponse(saved);
    }

    /**
     * 장바구니 항목 수량 수정
     * @param email
     * @param cartItemId
     * @param request
     * @return
     */
    @Transactional
    public CartItemResponse updateItemQuantity(String email, Long cartItemId, CartItemUpdateRequest request) {
        // 장바구니 항목 조회 및 사용자 검증
        CartItem cartItem = getCartItemForUser(email, cartItemId);
        cartItem.setCount(request.getQuantity());
        // 장바구니 항목 저장
        cartItemRepository.save(cartItem);
        return toResponse(cartItem);
    }

    /**
     * 장바구니 항목 삭제
     * @param email
     * @param cartItemId
     */
    @Transactional
    public void removeItem(String email, Long cartItemId) {
        // 장바구니 항목 조회 및 사용자 검증
        CartItem cartItem = getCartItemForUser(email, cartItemId);
        cartItemRepository.delete(cartItem);
    }
    
    /**
     * 장바구니 비우기
     * @param email
     */
    @Transactional
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        // 사용자 장바구니 조회
        cartRepository.findByUser(user).ifPresent(cart -> {
            if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                cartItemRepository.deleteAll(cart.getCartItems());
                cart.getCartItems().clear();
                cartRepository.save(cart);
            }
        });
    }

    /**
     * 이메일로 사용자 조회
     * @param email
     * @return
     */
    private User getUserByEmail(String email) {
        // 사용자 조회
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 카트 초기화
     * @param user
     * @return
     */
    private Cart initializeCart(User user) {
        // 새로운 카트 생성
        Cart cart = Cart.builder()
                .user(user)
                .cartItems(new ArrayList<>())
                .build();
        user.setCart(cart);
        return cartRepository.save(cart);
    }

    /**
     * CartItem 가져오기 및 사용자 검증
     * @param email
     * @param cartItemId
     * @return
     */
    private CartItem getCartItemForUser(String email, Long cartItemId) {
        // 사용자 조회
        User user = getUserByEmail(email);

        // CartItem 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        // 사용자 소유 여부 검증
        if (cartItem.getCart() == null || cartItem.getCart().getUser() == null
                || !cartItem.getCart().getUser().getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("해당 장바구니 항목에 대한 권한이 없습니다.");
        }

        return cartItem;
    }

    /**
     * CartItem 를 CartItemResponse 로 변환
     * @param cartItem
     * @return
     */
    private CartItemResponse toResponse(CartItem cartItem) {
        // 상품 정보 가져오기
        Product product = cartItem.getProduct();

        // 소계 계산
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
