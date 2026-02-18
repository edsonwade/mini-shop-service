package code.with.vanilson.market.notifications.infrastructure;

import code.with.vanilson.market.notifications.application.NotificationSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@minimarket.com}")
    private String fromEmail;

    @Value("${spring.mail.properties.mail.smtp.from:Mini Market}")
    private String fromName;

    @Override
    public void sendEmail(String recipient, String subject, String content) {
        log.info("Sending email to: {}", recipient);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true); // true = html

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", recipient);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}", recipient, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
