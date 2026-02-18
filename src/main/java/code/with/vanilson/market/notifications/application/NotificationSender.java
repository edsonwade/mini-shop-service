package code.with.vanilson.market.notifications.application;

public interface NotificationSender {
    void sendEmail(String recipient, String subject, String content);
}
