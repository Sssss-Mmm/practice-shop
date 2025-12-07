package com.example.practice_shop.dtos.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QueueStatusResponse {
    private boolean ready;
    private long position;
}
