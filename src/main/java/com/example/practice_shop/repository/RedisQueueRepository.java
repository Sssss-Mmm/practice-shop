package com.example.practice_shop.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepository implements QueueRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String QUEUE_KEY_PREFIX = "queue:"; // 큐 키
    private static final String READY_KEY_PREFIX = "queue:ready:"; // 허용 토큰 (토큰당 키 1개 + 개별 TTL)
    private static final String TOKEN_KEY_PREFIX = "queue:token:"; // 토큰 메타 정보

    @Override
    public void addToQueue(Long eventId, String token, long score) {
        String queueKey = queueKey(eventId);
        redisTemplate.opsForZSet().add(queueKey, token, score);
    }

    @Override
    public void saveTokenMeta(String token, Map<String, String> meta) {
        redisTemplate.opsForHash().putAll(tokenKey(token), meta);
    }

    @Override
    public Long getRank(Long eventId, String token) {
        String queueKey = queueKey(eventId);
        return redisTemplate.opsForZSet().rank(queueKey, token);
    }

    @Override
    public Map<Object, Object> getTokenMeta(String token) {
        return redisTemplate.opsForHash().entries(tokenKey(token));
    }

    @Override
    public boolean isReady(Long eventId, String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(readyKey(eventId, token)));
    }

    @Override
    public Set<String> getTokensInRange(Long eventId, long start, long end) {
        return redisTemplate.opsForZSet().range(queueKey(eventId), start, end);
    }

    @Override
    public void removeFromQueue(Long eventId, String token) {
        redisTemplate.opsForZSet().remove(queueKey(eventId), token);
    }

    @Override
    public void markReady(Long eventId, String token, Duration ttl) {
        // SET+EX 한 번으로 끝낸다. SADD 후 EXPIRE로 나누면 (1) 그 사이 죽었을 때 TTL이 안 붙고
        // (2) 이벤트 공유 키라 입장 허용이 일어날 때마다 먼저 들어온 토큰의 TTL까지 연장된다.
        redisTemplate.opsForValue().set(readyKey(eventId, token), "1", ttl);
    }

    @Override
    public void removeReady(Long eventId, String token) {
        redisTemplate.delete(readyKey(eventId, token));
    }

    @Override
    public Set<String> scanQueueKeys(int count) {
        Set<String> keys = new HashSet<>();
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(QUEUE_KEY_PREFIX + "[0-9]*")
                .count(count)
                .build();
        
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    private String queueKey(Long eventId) {
        return QUEUE_KEY_PREFIX + eventId;
    }

    private String readyKey(Long eventId, String token) {
        return READY_KEY_PREFIX + eventId + ":" + token;
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
