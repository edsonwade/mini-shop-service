package code.with.vanilson.market.orders.infrastructure;

import code.with.vanilson.market.orders.domain.Order;
import code.with.vanilson.market.orders.domain.OrderRepository;
import code.with.vanilson.market.shared.domain.PaymentEvents;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payments.captured", groupId = "orders-group")
    @Transactional
    public void handlePaymentCaptured(PaymentEvents.PaymentCapturedEvent event) {
        log.info("Payment captured for Order: {}", event.getOrderId());
        updateOrderStatus(event.getOrderId(), true);
    }

    @KafkaListener(topics = "payments.failed", groupId = "orders-group")
    @Transactional
    public void handlePaymentFailed(PaymentEvents.PaymentFailedEvent event) {
        log.warn("Payment failed for Order: {}. Reason: {}", event.getOrderId(), event.getReason());
        updateOrderStatus(event.getOrderId(), false);
    }

    private void updateOrderStatus(UUID orderId, boolean paid) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (paid) {
                order.confirmPayment();
            } else {
                order.cancel();
            }
            orderRepository.save(order);
            log.info("Updated order {} status to {}", orderId, order.getStatus());
        });
    }
}
