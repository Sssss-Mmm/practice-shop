package com.example.practice_shop.dtos.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QueueEnterResponse {
    private String token;
    private long position;
}
