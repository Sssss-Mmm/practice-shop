package com.example.practice_shop.repository;

import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser_Email(String email);

    // 예약 상태를 바꾸는 세 경로(사용자 취소 / 결제 승인 / 홀드 만료 회수)가 같은 행을 두고 경합하므로
    // 행 락으로 직렬화한다. 락을 기다린 쪽은 바뀐 상태를 보고 판단한다. 트랜잭션 안에서만 호출할 것.
    // users 조인이 들어간 쿼리(findByIdAndUser_Email)에 락을 걸면 Hibernate가 follow-on locking으로
    // "락 없이 조회 → 나중에 id로 FOR UPDATE" 하므로 손에 든 엔티티가 낡은 상태가 된다. 조인 없이 id로만 잠근다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findWithLockById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findByOrderId(String orderId);

    /** 결제 대기 상태로 방치된 예약 (좌석 홀드 만료 회수용). 락 대기 후 상태가 바뀐 행은 Postgres가 WHERE를 재평가해 제외한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservation> findByStatusAndReservedAtBefore(ReservationStatus status, LocalDateTime cutoff);

    /** 승인 결과를 모른 채 오래 남은 예약 (대사용). 읽기만 하므로 락 없음 — 실제 변경은 findByOrderId로 다시 잠근다. */
    List<Reservation> findByStatusAndConfirmRequestedAtBefore(ReservationStatus status, LocalDateTime cutoff);
}