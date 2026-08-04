package com.example.practice_shop.service;

import com.example.practice_shop.dtos.ticketing.SeatStatusMessage;
import com.example.practice_shop.entity.SeatInventory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        // 메시지는 지금 만든다. 커밋 이후엔 영속성 컨텍스트가 닫혀 지연 로딩(getSeat())이 터진다.
        SeatStatusMessage message = SeatStatusMessage.builder()
                .showtimeId(showtimeId)
                .seats(inventories.stream().map(this::toSeatStatusItem).toList())
                .build();

        afterCommit(() -> messagingTemplate.convertAndSend(topic(showtimeId), message));
    }

    /**
     * 전송을 커밋 이후로 미룹니다. 트랜잭션이 롤백되면 브로드캐스트도 나가지 않습니다.
     * (트랜잭션 밖에서 호출되면 되돌릴 것이 없으므로 즉시 전송)
     */
    private void afterCommit(Runnable send) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send.run();
            }
        });
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
