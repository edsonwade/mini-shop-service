package code.with.vanilson.market.events.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void publish(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, event).get(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while sending to Kafka", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Failed to send message to Kafka topic: " + topic + ". Cause: " + e.getCause().getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error sending message to Kafka topic: " + topic, e);
        }
    }
}
