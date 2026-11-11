package com.example.practice_shop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_IN_USE(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "U003", "이메일 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "U004", "접근 권한이 없습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "U005", "이메일 인증이 완료되지 않았습니다."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "U006", "비활성화된 계정입니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "U007", "로그인이 필요합니다."),

    // Ticketing
    SHOWTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "해당 회차 정보를 찾을 수 없습니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "T002", "선택한 좌석 정보를 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "T003", "이미 예약된 좌석입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "T004", "예매 정보를 찾을 수 없습니다."),
    ALREADY_CANCELLED_RESERVATION(HttpStatus.BAD_REQUEST, "T005", "이미 취소된 예매입니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "T006", "결제 금액이 일치하지 않습니다."),
    PAYMENT_CONFIRM_IN_PROGRESS(HttpStatus.CONFLICT, "T007", "결제 확인 중인 예매는 취소할 수 없습니다. 잠시 후 다시 시도해 주세요."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
