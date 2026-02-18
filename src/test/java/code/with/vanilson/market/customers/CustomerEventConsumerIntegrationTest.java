package code.with.vanilson.market.customers;

import code.with.vanilson.market.customers.domain.Customer;
import code.with.vanilson.market.customers.domain.CustomerRepository;
import code.with.vanilson.market.shared.domain.UserRegisteredEvent;
import code.with.vanilson.market.shared.infrastructure.test.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerEventConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void consuming_user_registered_event_creates_customer_profile() throws Exception {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(java.util.UUID.randomUUID())
                .email("event.user@example.com")
                .tenantId("tenant-x")
                .name("Event User")
                .build();

        kafkaTemplate.send("user.registered", event).get();

        // Wait briefly for consumer to process
        Thread.sleep(Duration.ofSeconds(2).toMillis());

        Optional<Customer> created = customerRepository.findByEmail("event.user@example.com");
        assertThat(created).isPresent();
        assertThat(created.get().getName()).isEqualTo("Event User");
    }
}
