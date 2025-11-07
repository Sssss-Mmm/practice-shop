package com.example.practice_shop.service;

import com.example.practice_shop.constant.OrderStatus;
import com.example.practice_shop.constant.PaymentStatus;
import com.example.practice_shop.dtos.Order.OrderCreateRequest;
import com.example.practice_shop.dtos.Order.OrderItemResponse;
import com.example.practice_shop.dtos.Order.OrderResponse;
import com.example.practice_shop.dtos.common.PagedResponse;
import com.example.practice_shop.entity.Cart;
import com.example.practice_shop.entity.CartItem;
import com.example.practice_shop.entity.Order;
import com.example.practice_shop.entity.OrderItem;
import com.example.practice_shop.entity.Product;
import com.example.practice_shop.entity.User;
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
                .paymentStatus(PaymentStatus.PAID)
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
            product.setStockQuantity(product.getStockQuantity() - quantity);

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

        // 장바구니 비우기
        cartService.clearCart(email);

        return toResponse(savedOrder);
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
}
