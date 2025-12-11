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
    /**
     * HTTP 통신을 위한 RestTemplate
     */
    private final RestTemplate restTemplate;

    /**
     * 토스 시크릿 키
     */
    @Value("${toss.payments.secret-key:}")
    private String secretKey;

    /**
     * 토스 결제 승인 URL
     */
    @Value("${toss.payments.confirm-url:https://api.tosspayments.com/v1/payments/confirm}")
    private String confirmUrl;

    /**
     * 토스 결제 승인 요청
     * @param request 결제 승인 요청 정보
     * @return 결제 승인 응답 정보
     */
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

    /**
     * 토스 결제 취소 요청
     * @param paymentKey 결제 키
     * @param cancelReason 취소 사유
     */
    public void cancelPayment(String paymentKey, String cancelReason) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("토스 시크릿 키가 설정되지 않았습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader());

        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        String body = String.format("{\"cancelReason\": \"%s\"}", cancelReason);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException("토스 결제 취소 실패: " + ex.getResponseBodyAsString(), ex);
        }
    }

    /**
     * Basic Auth 헤더 생성
     * @return
     */
    private String buildBasicAuthHeader() {
        String credential = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
