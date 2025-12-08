package com.example.practice_shop.service;

import com.example.practice_shop.dtos.queue.QueueEnterResponse;
import com.example.practice_shop.dtos.queue.QueueStatusResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class QueueService {

    /**
     * Redis 기반 대기열을 관리하는 서비스.
     * - queue:{eventId}: 정렬집합으로 순번 관리
     * - queue:ready:{eventId}: 입장 허용 토큰 모음
     * - queue:token:{token}: 토큰 메타 정보(eventId, userId 등)
     */
    private static final String QUEUE_KEY_PREFIX = "queue:";
    private static final String READY_KEY_PREFIX = "queue:ready:";
    private static final String TOKEN_KEY_PREFIX = "queue:token:";
    private static final Duration READY_TTL = Duration.ofMinutes(5);
    private static final int DEFAULT_ALLOW_PER_TICK = 10;

    private final StringRedisTemplate redisTemplate;

    public QueueEnterResponse enter(Long eventId, String userId) {
        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        String queueKey = queueKey(eventId);

        redisTemplate.opsForZSet().add(queueKey, token, now);
        redisTemplate.opsForHash().putAll(tokenKey(token), Map.of(
                "eventId", eventId.toString(),
                "userId", userId != null ? userId : "anonymous",
                "createdAt", Long.toString(now)
        ));

        Long rank = redisTemplate.opsForZSet().rank(queueKey, token);
        long position = rank == null ? -1 : rank + 1;
        return QueueEnterResponse.builder()
                .token(token)
                .position(position)
                .build();
    }

    public QueueStatusResponse status(String token) {
        Map<Object, Object> meta = redisTemplate.opsForHash().entries(tokenKey(token));
        if (CollectionUtils.isEmpty(meta)) {
            throw new IllegalArgumentException("유효하지 않은 대기열 토큰입니다.");
        }
        Long eventId = Long.valueOf((String) meta.get("eventId"));
        String queueKey = queueKey(eventId);
        Long rank = redisTemplate.opsForZSet().rank(queueKey, token);
        long position = rank == null ? -1 : rank + 1;
        boolean ready = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(readyKey(eventId), token));
        return QueueStatusResponse.builder()
                .ready(ready)
                .position(position)
                .build();
    }

    public void allowEntriesForEvent(Long eventId, int allowCount) {
        String queueKey = queueKey(eventId);
        Set<String> tokens = redisTemplate.opsForZSet().range(queueKey, 0, allowCount - 1);
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        tokens.forEach(token -> {
            redisTemplate.opsForZSet().remove(queueKey, token);
            markReady(eventId, token);
        });
    }

    public Set<String> listQueueKeys() {
        // queue:{eventId} 형태만 가져오도록 패턴 제한
        return redisTemplate.keys(QUEUE_KEY_PREFIX + "[0-9]*");
    }

    private void markReady(Long eventId, String token) {
        String readyKey = readyKey(eventId);
        redisTemplate.opsForSet().add(readyKey, token);
        redisTemplate.expire(readyKey, READY_TTL);
    }

    private String queueKey(Long eventId) {
        return QUEUE_KEY_PREFIX + eventId;
    }

    private String readyKey(Long eventId) {
        return READY_KEY_PREFIX + eventId;
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    public int defaultAllowCount() {
        return DEFAULT_ALLOW_PER_TICK;
    }
}
