package code.with.vanilson.market.payments.infrastructure;

import code.with.vanilson.market.events.domain.EventProducer;
import code.with.vanilson.market.payments.domain.Payment;
import code.with.vanilson.market.payments.domain.PaymentRepository;
import code.with.vanilson.market.payments.domain.PaymentStatus;
import code.with.vanilson.market.shared.domain.Money;
import code.with.vanilson.market.shared.domain.OrderPlacedEvent;
import code.with.vanilson.market.shared.domain.PaymentEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentRepository paymentRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders.placed", groupId = "payments-group")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Processing payment for Order: {}", event.getOrderId());

        // Simulate Payment Processing
        boolean success = simulatePayment(); // In real app, call Gateway

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount(new Money(event.getTotalAmount(), event.getCurrency()))
                .status(success ? PaymentStatus.CAPTURED : PaymentStatus.FAILED)
                .build();

        payment = paymentRepository.save(payment);

        if (success) {
            eventProducer.publish("payments.captured", PaymentEvents.PaymentCapturedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(event.getOrderId())
                    .amount(event.getTotalAmount())
                    .currency(event.getCurrency())
                    .build());
        } else {
            eventProducer.publish("payments.failed", PaymentEvents.PaymentFailedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(event.getOrderId())
                    .reason("Mock payment failure")
                    .build());
        }
    }

    private boolean simulatePayment() {
        return true; // placeholder
    }

}
