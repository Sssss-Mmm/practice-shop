package com.example.practice_shop.repository;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

public interface QueueRepository {
    void addToQueue(Long eventId, String token, long score);
    void saveTokenMeta(String token, Map<String, String> meta);
    Long getRank(Long eventId, String token);
    Map<Object, Object> getTokenMeta(String token);
    boolean isReady(Long eventId, String token);
    Set<String> getTokensInRange(Long eventId, long start, long end);
    void removeFromQueue(Long eventId, String token);
    void markReady(Long eventId, String token, Duration ttl);
    Set<String> scanQueueKeys(int count);
}
