package com.example.practice_shop.service;

import com.example.practice_shop.dtos.ticketing.ReservationResponse;
import com.example.practice_shop.dtos.ticketing.SeatSelectionRequest;

import java.util.List;

public interface TicketingService {
    ReservationResponse createReservation(String email, SeatSelectionRequest request);
    List<ReservationResponse> getUserReservations(String email);
    void cancelReservation(String email, Long reservationId);
}