package com.example.practice_shop.repository;

import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Showtime;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByEventAndStartDateTimeAfter(Event event, LocalDateTime dateTime);
}
