package code.with.vanilson.market.notifications.infrastructure;

import code.with.vanilson.market.notifications.application.NotificationSender;
import code.with.vanilson.market.notifications.domain.NotificationLog;
import code.with.vanilson.market.notifications.domain.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationLogRepository repository;
    private final NotificationSender notificationSender;

    @KafkaListener(topics = "orders.placed", groupId = "market-group")
    public void handleOrderPlaced(String orderEventJson) {
        log.info("Received order placed event: {}", orderEventJson);

        // Send Email
        String recipient = "customer@example.com"; // In real app, extract from JSON
        String subject = "Order Placed";
        notificationSender.sendEmail(recipient, subject, orderEventJson);

        NotificationLog logEntry = NotificationLog.builder()
                .type("EMAIL")
                .recipient(recipient) // extracted from event
                .subject(subject)
                .content(orderEventJson)
                .sentAt(Instant.now())
                .status("SENT")
                .build();

        repository.save(logEntry);
    }
}
