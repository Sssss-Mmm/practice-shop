package com.example.practice_shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    @Bean
    public RestTemplate restTemplate() {
        // 타임아웃이 없으면 외부(Toss) 장애 시 요청 스레드가 무한정 묶이고, 승인 실패 처리 경로도 돌지 않는다.
        // READ_TIMEOUT은 TicketingService.CONFIRM_RECONCILE_AFTER보다 짧아야 한다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return new RestTemplate(factory);
    }
}
