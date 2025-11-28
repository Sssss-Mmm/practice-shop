package com.example.practice_shop.service;

import com.example.practice_shop.dtos.ticketing.SeatStatusMessage;
import com.example.practice_shop.entity.SeatInventory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatStatuses(Long showtimeId, List<SeatInventory> inventories) {
        SeatStatusMessage message = SeatStatusMessage.builder()
                .showtimeId(showtimeId)
                .seats(inventories.stream().map(this::toSeatStatusItem).toList())
                .build();

        messagingTemplate.convertAndSend(topic(showtimeId), message);
    }

    public void broadcastSeatStatus(SeatInventory inventory) {
        if (inventory == null || inventory.getShowtime() == null) {
            return;
        }
        broadcastSeatStatuses(inventory.getShowtime().getId(), List.of(inventory));
    }

    private SeatStatusMessage.SeatStatusItem toSeatStatusItem(SeatInventory inventory) {
        return SeatStatusMessage.SeatStatusItem.builder()
                .seatInventoryId(inventory.getId())
                .seatId(inventory.getSeat() != null ? inventory.getSeat().getId() : null)
                .sectionName(inventory.getSeat() != null ? inventory.getSeat().getSectionName() : null)
                .rowLabel(inventory.getSeat() != null ? inventory.getSeat().getRowLabel() : null)
                .seatNumber(inventory.getSeat() != null ? inventory.getSeat().getSeatNumber() : null)
                .status(inventory.getStatus())
                .holdExpiresAt(inventory.getHoldExpiresAt())
                .build();
    }

    private String topic(Long showtimeId) {
        return "/topic/seat/" + showtimeId;
    }
}
