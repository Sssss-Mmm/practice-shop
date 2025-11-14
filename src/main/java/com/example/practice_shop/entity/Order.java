package com.example.practice_shop.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.practice_shop.constant.OrderStatus;
import com.example.practice_shop.constant.PaymentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter // Getter 자동 생성
@Setter // Setter 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
@Builder // 빌더 패턴 지원
@Table(name = "orders")
public class Order extends BaseTimeEntity {

     /** 주문 고유 아이디 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id" )
    private Long id;

    /** 주문 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    /** 결제 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    /** 총 주문 금액 */
    @Column(nullable = false)
    private BigDecimal totalPrice;

    /** 주문자 */
    @JoinColumn(name = "user_id")
    @ManyToOne(optional = false)
    private User user;

    /** 배송 정보 */
    private String shippingAddress;
    private String contactNumber;
    private String recipientName;
    private String postalCode;
    private String deliveryInstructions;

    /** 결제 정보 */
    private String paymentMethod;
    
    /** PG 결제 키 */
    @Column(length = 128)
    private String paymentKey;

    /** 주문 상품 목록 */
    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}
