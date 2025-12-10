package com.example.practice_shop.repository;

import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.entity.Seat;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;

import jakarta.persistence.LockModeType;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    List<SeatInventory> findByShowtimeAndStatus(Showtime showtime, SeatStatus status);
    List<SeatInventory> findBySeat(Seat seat);

    List<SeatInventory> findAllByIdInAndShowtimeId(List<Long> ids, Long showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select si from SeatInventory si where si.id in :ids and si.showtime.id = :showtimeId")
    List<SeatInventory> findAllByIdInAndShowtimeIdWithLock(List<Long> ids, Long showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select si from SeatInventory si where si.seat.id in :seatIds and si.showtime.id = :showtimeId")
    List<SeatInventory> findAllBySeatIdInAndShowtimeIdWithLock(List<Long> seatIds, Long showtimeId);
}
