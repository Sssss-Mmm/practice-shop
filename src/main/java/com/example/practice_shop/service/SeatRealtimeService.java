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

    /**
     * 좌석 상태를 브로드캐스팅합니다.
     * @param showtimeId
     * @param inventories
     */
    public void broadcastSeatStatuses(Long showtimeId, List<SeatInventory> inventories) {
        SeatStatusMessage message = SeatStatusMessage.builder()
                .showtimeId(showtimeId)
                .seats(inventories.stream().map(this::toSeatStatusItem).toList())
                .build();

        messagingTemplate.convertAndSend(topic(showtimeId), message);
    }

    /**
     * 좌석 상태를 브로드캐스팅합니다.
     * @param inventory
     */
    public void broadcastSeatStatus(SeatInventory inventory) {
        if (inventory == null || inventory.getShowtime() == null) {
            return;
        }
        broadcastSeatStatuses(inventory.getShowtime().getId(), List.of(inventory));
    }

    /**
     * 좌석 상태를 SeatStatusMessage.SeatStatusItem으로 변환합니다.
     * @param inventory
     * @return
     */
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

    /**
     * 좌석 상태를 브로드캐스팅할 토픽을 반환합니다.
     * @param showtimeId
     * @return
     */
    private String topic(Long showtimeId) {
        return "/topic/seat/" + showtimeId;
    }
}
