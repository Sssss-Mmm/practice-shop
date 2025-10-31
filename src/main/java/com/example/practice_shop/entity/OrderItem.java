package com.example.practice_shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class OrderItem extends BaseTimeEntity {
    
    /** 주문 상품 고유 아이디 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    
    /** 주문 고유 아이디 */
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    /** 제품 고유 아이디 */
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    /** 주문 수량 */
    @Column(nullable = false)
    private Integer count;

    /** 주문 가격 */
    @Column(nullable = false)
    private Double orderPrice; 

}
