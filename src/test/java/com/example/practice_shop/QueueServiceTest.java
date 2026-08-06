package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.practice_shop.dtos.queue.QueueStatusResponse;
import com.example.practice_shop.repository.QueueRepository;
import com.example.practice_shop.service.QueueService;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * TDD 연습용. status()의 position 계산 분기 3개를 사이클 순서대로 담았다.
 * Red -> Green 을 직접 보려면 QueueService.status()의 해당 줄을 지웠다 되돌리면 된다.
 */
class QueueServiceTest {

    private final QueueRepository repo = mock(QueueRepository.class);
    private final QueueService sut = new QueueService(repo);

    /** 토큰이 eventId=1 이벤트에 속한 상태로 세팅. */
    private void 토큰이_유효하면(String token) {
        // 토큰이 eventId=1 이벤트에 속한 상태로 세팅
        when(repo.getTokenMeta(token)).thenReturn(Map.<Object, Object>of("eventId", "1"));
    }

    @Test
    void 대기열_1번째면_position은_1이다() {
        토큰이_유효하면("t");
        when(repo.getRank(1L, "t")).thenReturn(0L); // Redis rank는 0부터

        QueueStatusResponse res = sut.status("t");

        assertThat(res.getPosition()).isEqualTo(1);
    }

    @Test
    void 대기열에_없는_토큰이면_position은_마이너스1이다() {
        토큰이_유효하면("t");
        when(repo.getRank(1L, "t")).thenReturn(null);

        assertThat(sut.status("t").getPosition()).isEqualTo(-1);
    }

    @Test
    void 메타가_없는_토큰이면_예외() {
        when(repo.getTokenMeta("없음")).thenReturn(Map.of());

        assertThatThrownBy(() -> sut.status("없음"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 허용된_토큰이면_ready가_true다() {
        토큰이_유효하면("t");
        when(repo.isReady(1L, "t")).thenReturn(true);

        assertThat(sut.status("t").isReady()).isTrue();
    }
}
