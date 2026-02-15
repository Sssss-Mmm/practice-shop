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
    private static final String READY_KEY_PREFIX = "queue:ready:"; // 허용 토큰 모음
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
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(readyKey(eventId), token));
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
        String readyKey = readyKey(eventId);
        redisTemplate.opsForSet().add(readyKey, token);
        redisTemplate.expire(readyKey, ttl);
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

    private String readyKey(Long eventId) {
        return READY_KEY_PREFIX + eventId;
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
