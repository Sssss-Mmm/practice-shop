package com.example.practice_shop.service;

import com.example.practice_shop.dtos.queue.QueueEnterResponse;
import com.example.practice_shop.dtos.queue.QueueStatusResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class QueueService {

    /**
     * Redis 기반 대기열을 관리하는 서비스.
     * 데이터 접근은 QueueRepository를 통해 수행합니다.
     */
    private static final Duration READY_TTL = Duration.ofMinutes(5); // 허용 토큰 유지 시간
    private static final int DEFAULT_ALLOW_PER_TICK = 300; // 허용 토큰 수

    private final com.example.practice_shop.repository.QueueRepository queueRepository;

    /**
     * 대기열에 입장합니다.
     * @param eventId
     * @param userId
     * @return
     */
    public QueueEnterResponse enter(Long eventId, String userId) {
        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        queueRepository.addToQueue(eventId, token, now);
        
        Map<String, String> meta = Map.of(
                "eventId", eventId.toString(),
                "userId", userId != null ? userId : "anonymous",
                "createdAt", Long.toString(now)
        );
        queueRepository.saveTokenMeta(token, meta);

        Long rank = queueRepository.getRank(eventId, token);
        long position = rank == null ? -1 : rank + 1;
        
        return QueueEnterResponse.builder()
                .token(token)
                .position(position)
                .build();
    }

    /**
     * 토큰의 상태를 조회합니다.
     * @param token
     * @return
     */
    public QueueStatusResponse status(String token) {
        Map<Object, Object> meta = queueRepository.getTokenMeta(token);
        if (CollectionUtils.isEmpty(meta)) {
            throw new IllegalArgumentException("유효하지 않은 대기열 토큰입니다.");
        }
        Long eventId = Long.valueOf((String) meta.get("eventId"));
        
        Long rank = queueRepository.getRank(eventId, token);
        long position = rank == null ? -1 : rank + 1;
        boolean ready = queueRepository.isReady(eventId, token);
        
        return QueueStatusResponse.builder()
                .ready(ready)
                .position(position)
                .build();
    }

    /**
     * 이벤트에 입장 허용을 합니다.
     * @param eventId
     * @param allowCount
     */
    public void allowEntriesForEvent(Long eventId, int allowCount) {
        Set<String> tokens = queueRepository.getTokensInRange(eventId, 0, allowCount - 1);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        tokens.forEach(token -> {
            queueRepository.removeFromQueue(eventId, token);
            queueRepository.markReady(eventId, token, READY_TTL);
        });
    }

    /**
     * 큐 키 목록을 가져옵니다.
     * Repository의 SCAN 기능을 사용합니다.
     * @return 큐 키 목록
     */
    public Set<String> listQueueKeys() {
        return queueRepository.scanQueueKeys(100);
    }

    /**
     * 기본 허용 수를 가져옵니다.
     * @return
     */
    public int defaultAllowCount() {
        return DEFAULT_ALLOW_PER_TICK;
    }
}
