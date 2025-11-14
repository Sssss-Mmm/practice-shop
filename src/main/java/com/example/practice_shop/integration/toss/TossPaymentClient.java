package com.example.practice_shop.integration.toss;

import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;
import com.example.practice_shop.dtos.Payment.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final RestTemplate restTemplate;

    @Value("${toss.payments.secret-key:}")
    private String secretKey;

    @Value("${toss.payments.confirm-url:https://api.tosspayments.com/v1/payments/confirm}")
    private String confirmUrl;

    public TossPaymentConfirmResponse confirmPayment(TossPaymentConfirmRequest request) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("토스 시크릿 키가 설정되지 않았습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader());

        HttpEntity<TossPaymentConfirmRequest> entity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<TossPaymentConfirmResponse> response = restTemplate.exchange(
                    confirmUrl,
                    HttpMethod.POST,
                    entity,
                    TossPaymentConfirmResponse.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("토스 결제 승인 실패: " + ex.getResponseBodyAsString(), ex);
        }
    }

    private String buildBasicAuthHeader() {
        String credential = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
