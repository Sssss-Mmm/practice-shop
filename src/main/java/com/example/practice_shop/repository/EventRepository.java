package com.example.practice_shop.repository;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.entity.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);
}
