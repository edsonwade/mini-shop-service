package code.with.vanilson.market.identity;

import code.with.vanilson.market.identity.domain.Role;
import code.with.vanilson.market.identity.domain.User;
import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.shared.infrastructure.test.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void persist_and_find_by_email() {
        User user = User.builder()
                .email("repo.user@example.com")
                .passwordHash("hash")
                .tenantId("t-1")
                .build();
        user.addRole(Role.CUSTOMER);

        user = userRepository.save(user);
        assertThat(user.getId()).isNotNull();

        Optional<User> found = userRepository.findByEmail("repo.user@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).contains(Role.CUSTOMER);
    }
}
