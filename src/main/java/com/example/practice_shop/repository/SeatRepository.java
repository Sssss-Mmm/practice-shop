package com.example.practice_shop.repository;

import com.example.practice_shop.entity.Seat;
import com.example.practice_shop.entity.Venue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenue(Venue venue);
}
