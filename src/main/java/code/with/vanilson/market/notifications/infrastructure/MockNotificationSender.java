package code.with.vanilson.market.notifications.infrastructure;

import code.with.vanilson.market.notifications.application.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockNotificationSender implements NotificationSender {
    @Override
    public void sendEmail(String recipient, String subject, String content) {
        log.info("MOCK EMAIL SENT to {}: Subject: {}, Content: {}", recipient, subject, content);
    }
}
