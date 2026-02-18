package code.with.vanilson.market.infrastructure;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name("orders.placed").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name("orders.cancelled").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderSettledTopic() {
        return TopicBuilder.name("orders.settled").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentCapturedTopic() {
        return TopicBuilder.name("payments.captured").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name("payments.failed").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentHoldTopic() {
        return TopicBuilder.name("payments.hold_requested").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic promotionRedeemedTopic() {
        return TopicBuilder.name("promotions.redeemed").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic notificationSentTopic() {
        return TopicBuilder.name("notifications.sent").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("user.registered").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name("audit.events").partitions(1).replicas(1).build();
    }
}
