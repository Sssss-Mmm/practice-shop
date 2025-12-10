package com.example.practice_shop.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 대기열에서 일정 수의 사용자를 입장 처리하는 간단한 워커입니다.
 * 운영 시 이벤트별 허용량/시간대를 조절할 수 있도록 확장하세요.
 */
@Component
@RequiredArgsConstructor
public class QueueWorker {

    private final QueueService queueService;

    /**
     * 주기적으로 큐를 스캔해 이벤트별로 일정 수량을 입장 허용한다.
     * 운영 정책에 맞게 allowEntriesForEvent 호출 파라미터를 확장하면 된다.
     */
    @Scheduled(fixedDelay = 2000)
    public void allowEntries() {
        Set<String> queueKeys = queueService.listQueueKeys();
        if (queueKeys == null || queueKeys.isEmpty()) {
            return;
        }
        queueKeys.forEach(key -> {
            try {
                Long eventId = extractEventId(key);
                queueService.allowEntriesForEvent(eventId, queueService.defaultAllowCount());
            } catch (Exception ignored) {
                // 키 파싱 실패 등은 건너뜁니다.
            }
        });
    }

    /**
     * 큐 키에서 이벤트 ID를 추출합니다.
     * @param key
     * @return
     */
    private Long extractEventId(String key) {
        // key 형식: queue:{eventId}
        int idx = key.indexOf(":");
        if (idx < 0 || idx + 1 >= key.length()) {
            throw new IllegalArgumentException("Invalid queue key: " + key);
        }
        return Long.parseLong(key.substring(idx + 1));
    }
}
