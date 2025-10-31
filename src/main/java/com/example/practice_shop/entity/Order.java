package com.example.practice_shop.entity;

import java.util.List;

import com.example.practice_shop.constant.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Order extends BaseTimeEntity {

     /** 주문 고유 아이디 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id" )
    private Long id;

    /** 주문 상태 */
    @Column(nullable = false)
    private OrderStatus orderStatus;

    /** 총 주문 금액 */
    @Column(nullable = false)
    private Double totalPrice;

    /** 사용자 고유 아이디 */
    @Column
    @JoinColumn(name = "user_id")
    @ManyToOne(optional = false)
    private User user;

    /** 배송 주소 */
    @Column(nullable = true)
    private String shippingAddress;

    /** 연락처 */
    @Column(nullable = true)
    private String contactNumber;

    /** 수령인 이름 */
    @Column(nullable = true)
    private String recipientName;

    /** 우편번호 */
    @Column(nullable = true)
    private String postalCode;

    /** 배송 요청 사항 */
    @Column(nullable = true)
    private String deliveryInstructions;
    
    /** 결제 방법 */
    @Column(nullable = true)
    private String paymentMethod;

    /** 주문 상품 목록 */
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;
}
