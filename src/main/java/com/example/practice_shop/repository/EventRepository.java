package com.example.practice_shop.repository;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.entity.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE " +
            "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR e.category = :category) AND " +
            "(:startDate IS NULL OR e.salesEndDate >= :startDate) AND " +
            "(:endDate IS NULL OR e.salesStartDate <= :endDate) AND " +
            "(:status IS NULL OR e.status = :status)")
    List<Event> searchEvents(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
            @org.springframework.data.repository.query.Param("status") EventStatus status);
}
