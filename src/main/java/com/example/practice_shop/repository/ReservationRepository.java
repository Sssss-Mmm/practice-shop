package com.example.practice_shop.repository;

import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.entity.Reservation;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByShowtimeAndStatus(Showtime showtime, ReservationStatus status);
    List<Reservation> findByStatusAndHoldExpiresAtBefore(ReservationStatus status, LocalDateTime cutoff);
}
