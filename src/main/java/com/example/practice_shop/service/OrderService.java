package com.example.practice_shop.service;

import com.example.practice_shop.constant.OrderStatus;
import com.example.practice_shop.constant.PaymentStatus;
import com.example.practice_shop.dtos.Order.OrderCreateRequest;
import com.example.practice_shop.dtos.Order.OrderItemResponse;
import com.example.practice_shop.dtos.Order.OrderResponse;
import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;
import com.example.practice_shop.dtos.Payment.TossPaymentConfirmResponse;
import com.example.practice_shop.dtos.common.PagedResponse;
import com.example.practice_shop.entity.Cart;
import com.example.practice_shop.entity.CartItem;
import com.example.practice_shop.entity.Order;
import com.example.practice_shop.entity.OrderItem;
import com.example.practice_shop.entity.Product;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.integration.toss.TossPaymentClient;
import com.example.practice_shop.repository.CartRepository;
import com.example.practice_shop.repository.OrderRepository;
import com.example.practice_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final TossPaymentClient tossPaymentClient;

    /**
     * 주문 생성
     * @param email
     * @param request
     * @return
     */
    @Transactional
    public OrderResponse createOrder(String email, OrderCreateRequest request) {
        User user = findUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("장바구니가 비어 있습니다."));

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다.");
        }

        Order order = Order.builder()
                .user(user)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .contactNumber(request.getContactNumber())
                .recipientName(request.getRecipientName())
                .postalCode(request.getPostalCode())
                .deliveryInstructions(request.getDeliveryInstructions())
                .paymentMethod(request.getPaymentMethod())
                .orderItems(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getCount();
            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("재고가 부족한 상품이 있습니다: " + product.getProductName());
            }

            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .count(quantity)
                    .orderPrice(unitPrice)
                    .build();

            order.getOrderItems().add(orderItem);
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    /**
     * 토스 결제 승인 처리
     * @param email
     * @param request
     * @return
     */
    @Transactional
    public OrderResponse confirmTossPayment(String email, TossPaymentConfirmRequest request) {
        User user = findUser(email);
        Long orderId = extractOrderId(request.getOrderId());
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        // 이미 결제가 완료된 주문인 경우 바로 응답 반환
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return toResponse(order);
        }
    
        // 결제 금액 검증
        validatePaymentAmount(order, request.getAmount());

        try {
            TossPaymentConfirmResponse tossResponse = tossPaymentClient.confirmPayment(request);
            deductStock(order);
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setOrderStatus(OrderStatus.PROCESSING);
            order.setPaymentKey(tossResponse.getPaymentKey());
            orderRepository.save(order);

            long increment = order.getTotalPrice().longValue();
            user.accumulateSpend(increment);
            userRepository.save(user);

            cartService.clearCart(email);
            return toResponse(order);
        } catch (RuntimeException ex) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            throw ex;
        }
    }
    
    /**
     * 주문 목록 조회
     * @param email
     * @param page
     * @param size
     * @return
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrders(String email, int page, int size) {
        User user = findUser(email);
        int validatedPage = Math.max(page, 0);
        int validatedSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageRequest = PageRequest.of(validatedPage, validatedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUserOrderByCreatedAtDesc(user, pageRequest);

        List<OrderResponse> content = orderPage.stream()
                .map(this::toResponse)
                .toList();

        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    /**
     * 주문 상세 조회
     * @param email
     * @param orderId
     * @return
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String email, Long orderId) {
        User user = findUser(email);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        return toResponse(order);
    }
    
    /**
     * 주문 취소
     * @param email
     * @param orderId
     */
    @Transactional
    public void cancelOrder(String email, Long orderId) {
        User user = findUser(email);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        order.setOrderStatus(OrderStatus.CANCELED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
    }

    /**
     * 사용자 조회
     * @param email
     * @return
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * Order 를 OrderResponse 로 변환
     * @param order
     * @return
     */
    private OrderResponse toResponse(Order order) {
        // 주문 아이템 응답 리스트 생성
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(orderItem -> {
                    int quantity = orderItem.getCount();
                    int unitPrice = orderItem.getProduct().getPrice();
                    return OrderItemResponse.builder()
                            .orderItemId(orderItem.getId())
                            .productId(orderItem.getProduct().getId())
                            .productName(orderItem.getProduct().getProductName())
                            .price(unitPrice)
                            .quantity(quantity)
                            .lineTotal(unitPrice * quantity)
                            .imageUrl(orderItem.getProduct().getImageUrl())
                            .build();
                })
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalPrice(order.getTotalPrice())
                .shippingAddress(order.getShippingAddress())
                .contactNumber(order.getContactNumber())
                .recipientName(order.getRecipientName())
                .postalCode(order.getPostalCode())
                .deliveryInstructions(order.getDeliveryInstructions())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
    /**
     * 재고 차감
     * @param order
     */
    private void deductStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            int quantity = orderItem.getCount();
            if (product.getStockQuantity() < quantity) {
                throw new IllegalStateException("결제 처리 중 품절된 상품이 있습니다: " + product.getProductName());
            }
            product.setStockQuantity(product.getStockQuantity() - quantity);
        }
    }

    /**
     * 결제 금액 검증
     * @param order
     * @param paidAmount
     */
    private void validatePaymentAmount(Order order, Long paidAmount) {
        if (paidAmount == null) {
            throw new IllegalArgumentException("결제 금액이 필요합니다.");
        }
        BigDecimal expected = order.getTotalPrice();
        if (expected == null) {
            throw new IllegalStateException("주문 금액이 설정되지 않았습니다.");
        }
        if (expected.compareTo(BigDecimal.valueOf(paidAmount)) != 0) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }
    }
    /**
     * 주문 번호에서 실제 주문 ID 추출
     * @param orderIdToken
     * @return
     */
    private Long extractOrderId(String orderIdToken) {
        if (orderIdToken == null || orderIdToken.isBlank()) {
            throw new IllegalArgumentException("주문 번호가 필요합니다.");
        }
        try {
            String normalized = orderIdToken.trim();
            if (normalized.toUpperCase().startsWith("ORD-")) {
                normalized = normalized.substring(4);
            }
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("유효하지 않은 주문 번호 형식입니다.");
        }
    }
}
