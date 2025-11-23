package com.example.practice_shop.repository;

import com.example.practice_shop.constant.TicketStatus;
import com.example.practice_shop.entity.Reservation;
import com.example.practice_shop.entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByReservation(Reservation reservation);
    List<Ticket> findByStatus(TicketStatus status);
}
