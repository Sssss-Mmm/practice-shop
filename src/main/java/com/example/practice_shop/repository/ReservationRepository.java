package com.example.practice_shop.repository;

import com.example.practice_shop.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser_Email(String email);
    Optional<Reservation> findByIdAndUser_Email(Long id, String email);
    Optional<Reservation> findByOrderId(String orderId);
}