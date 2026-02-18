package code.with.vanilson.market.customers.infrastructure;

import code.with.vanilson.market.customers.api.CustomerDto;
import code.with.vanilson.market.customers.application.CustomerService;
import code.with.vanilson.market.shared.domain.UserRegisteredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerEventConsumer {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    public CustomerEventConsumer(CustomerService customerService, ObjectMapper objectMapper) {
        this.customerService = customerService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user.registered", groupId = "customers-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent: {}", event);

        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest();
        request.setTenantId(event.getTenantId());
        request.setName(event.getName());
        request.setEmail(event.getEmail());
        // Phone is optional/unknown at this stage

        try {
            customerService.createCustomer(request);
            log.info("Automatically created customer profile for {}", event.getEmail());
        } catch (Exception e) {
            log.warn("Customer profile likely exists or creation failed: {}", e.getMessage());
        }
    }
}
