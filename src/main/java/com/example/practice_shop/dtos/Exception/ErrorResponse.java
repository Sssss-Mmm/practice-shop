package com.example.practice_shop.dtos.Exception;

import lombok.Getter;

import java.time.LocalDateTime;

import com.example.practice_shop.exception.ErrorCode;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String code;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 커스텀 메시지를 사용하는 생성자
     * @param errorCode 에러 코드
     * @param customMessage 커스텀 에러 메시지
     */
    public ErrorResponse(ErrorCode errorCode, String customMessage) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.code = errorCode.getCode();
        this.message = customMessage != null ? customMessage : errorCode.getMessage();
    }
}

