package com.example.practice_shop.service.payment;

import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;
import com.example.practice_shop.service.TicketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketPaymentProcessor implements PaymentProcessorStrategy {

    private final TicketingService ticketingService;

    @Override
    public boolean supports(String orderId) {
        return orderId.startsWith("tck-");
    }

    @Override
    public void process(TossPaymentConfirmRequest request) {
        // TicketingService.confirmPayment(String orderId, String paymentKey, Long amount)
        ticketingService.confirmPayment(request.getOrderId(), request.getPaymentKey(), request.getAmount());
    }
}
