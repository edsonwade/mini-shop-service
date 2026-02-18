package code.with.vanilson.market.customers;

import code.with.vanilson.market.customers.domain.Customer;
import code.with.vanilson.market.customers.domain.CustomerRepository;
import code.with.vanilson.market.shared.infrastructure.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void create_read_update_delete_customer() {
        Customer c = Customer.builder()
                .tenantId("t-1")
                .name("Repo Customer")
                .email("repo.customer@example.com")
                .phone("+15555555555")
                .build();

        // create
        c = customerRepository.save(c);
        assertThat(c.getId()).isNotNull();

        // read
        Optional<Customer> found = customerRepository.findByEmail("repo.customer@example.com");
        assertThat(found).isPresent();

        // update
        Customer toUpdate = found.get();
        toUpdate.setName("Repo Customer Updated");
        customerRepository.save(toUpdate);

        Customer afterUpdate = customerRepository.findById(toUpdate.getId()).orElseThrow();
        assertThat(afterUpdate.getName()).isEqualTo("Repo Customer Updated");

        // delete
        customerRepository.delete(afterUpdate);
        assertThat(customerRepository.findById(afterUpdate.getId())).isEmpty();
    }
}
