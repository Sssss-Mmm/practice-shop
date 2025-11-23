package com.example.practice_shop.repository;

import com.example.practice_shop.constant.PaymentStatus;
import com.example.practice_shop.entity.Payment;
import com.example.practice_shop.entity.Reservation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservation(Reservation reservation);
    Optional<Payment> findByPaymentKey(String paymentKey);
    long countByStatus(PaymentStatus status);
}
