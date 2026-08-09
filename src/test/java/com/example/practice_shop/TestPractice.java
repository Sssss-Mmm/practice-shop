package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TDD 연습: 대기열 예상 대기시간 계산기
 *
 * 요구사항 (이대로 하나씩 RED -> GREEN -> REFACTOR)
 *  1. rank(내 앞 대기 인원)가 0이면 0분
 *  2. rank 20, 분당 처리 10명 -> 2분
 *  3. rank 5, 분당 처리 10명 -> 1분 (올림. 0분이라고 하면 안 됨)
 *  4. throughput <= 0 이면 IllegalArgumentException
 *  5. rank < 0 이면 IllegalArgumentException
 *
 * 규칙: 테스트 먼저 작성 -> 실패 확인 -> 통과하는 최소 코드만 작성.
 * 아래 1번은 예시로 채워뒀다. 2~5번은 직접 작성할 것.
 * 실행: ./gradlew test --tests '*TestPractice'
 */
class TestPractice {

    @Test
    @DisplayName("앞에 아무도 없으면 대기시간은 0분")
    void noOneAhead() {
        assertThat(WaitTime.estimateMinutes(0, 10)).isZero();
        
    }

    // TODO 2. rank 20, throughput 10 -> 2분
    @Test
    @DisplayName("rank 20, throughput 10 -> 2분")
    void rank20Throughput10() {
        /**
         * rank 20, throughput 10 -> 2분
         */
        assertThat(WaitTime.estimateMinutes(20, 10)).isEqualTo(2);
    }
    // TODO 3. rank 5, throughput 10 -> 1분 (올림)
    @Test
    @DisplayName("rank 5, throughput 10 -> 1분 (올림)")
    void rank5Throughput10() {
        assertThat(WaitTime.estimateMinutes(5, 10)).isEqualTo(1);
    }

    @Test
    @DisplayName("처리량이 0 이하면 예외")
    void invalidThroughput() {
        assertThatThrownBy(() -> WaitTime.estimateMinutes(10, 0))
                .isInstanceOf(IllegalArgumentException.class);
        // TODO 음수 throughput 도 추가
        assertThatThrownBy(() -> WaitTime.estimateMinutes(10, -5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rank 가 음수면 예외")
    void invalidRank() {
        assertThatThrownBy(() -> WaitTime.estimateMinutes(-1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** 연습용 프로덕션 코드. 통과하는 최소한만 채울 것. */
    static class WaitTime {
        static int estimateMinutes(int rank, int throughputPerMinute) {
            if (throughputPerMinute <= 0) {
                throw new IllegalArgumentException("throughputPerMinute must be positive");
            }
            if (rank < 0) {
                throw new IllegalArgumentException("rank must be non-negative");
            }
            return (int) Math.ceil((double) rank / throughputPerMinute);
        }

        
    }
}
