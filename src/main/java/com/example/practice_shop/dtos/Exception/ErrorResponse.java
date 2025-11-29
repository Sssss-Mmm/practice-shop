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
}
