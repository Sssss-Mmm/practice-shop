package com.example.practice_shop.repository;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.entity.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByStatus(EventStatus status);

    /**
     * N+1 문제 해결: Venue와 Showtimes를 Fetch Join으로 한 번에 조회
     * DISTINCT 사용으로 중복 제거
     */
    @Query("SELECT DISTINCT e FROM Event e " +
            "LEFT JOIN FETCH e.venue " +
            "LEFT JOIN FETCH e.showtimes s " +
            "LEFT JOIN FETCH s.venue " +
            "WHERE (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "       OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR e.category = :category) AND " +
            "(:startDate IS NULL OR e.salesEndDate >= :startDate) AND " +
            "(:endDate IS NULL OR e.salesStartDate <= :endDate) AND " +
            "(:status IS NULL OR e.status = :status)")
    List<Event> searchEvents(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("status") EventStatus status);

    /**
     * 단일 이벤트 조회 시에도 연관 엔티티를 함께 로드
     */
    @EntityGraph(attributePaths = {"venue", "showtimes", "showtimes.venue"})
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findByIdWithDetails(@Param("eventId") Long eventId);
}
