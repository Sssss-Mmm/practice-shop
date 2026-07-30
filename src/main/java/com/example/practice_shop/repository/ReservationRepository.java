package com.example.practice_shop.repository;

import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser_Email(String email);
    Optional<Reservation> findByIdAndUser_Email(Long id, String email);
    Optional<Reservation> findByOrderId(String orderId);
    /** 결제 대기 상태로 방치된 예약 (좌석 홀드 만료 회수용) */
    List<Reservation> findByStatusAndReservedAtBefore(ReservationStatus status, LocalDateTime cutoff);
}