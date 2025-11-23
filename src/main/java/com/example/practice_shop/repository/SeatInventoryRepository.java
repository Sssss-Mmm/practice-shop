package com.example.practice_shop.repository;

import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.entity.Seat;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    List<SeatInventory> findByShowtimeAndStatus(Showtime showtime, SeatStatus status);
    List<SeatInventory> findBySeat(Seat seat);
}
