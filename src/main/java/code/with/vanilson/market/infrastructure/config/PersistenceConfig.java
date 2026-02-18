package code.with.vanilson.market.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "code.with.vanilson.market")
@EnableMongoRepositories(basePackages = "code.with.vanilson.market")
public class PersistenceConfig {
}
