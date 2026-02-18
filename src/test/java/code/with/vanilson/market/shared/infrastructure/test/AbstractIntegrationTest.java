package code.with.vanilson.market.shared.infrastructure.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("market_test")
            .withUsername("postgres")
            .withPassword("password");

    static final GenericContainer<?> mongo = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
            .withExposedPorts(27017);

    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "redis_password");

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

    static {
        postgres.start();
        mongo.start();
        redis.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.mongodb.uri", () -> "mongodb://" + mongo.getHost() + ":" + mongo.getMappedPort(27017) + "/test");

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "redis_password");

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.admin.properties.allow.auto.create.topics", () -> "true");
        
        // JWT configuration for tests
        registry.add("app.jwt.secret", () -> "9a4f4e35455a5b5c5d5e5f606162636465666768696a6b6c6d6e6f7071727374"); // 256-bit secret
        registry.add("app.jwt.expiration", () -> 3600000); // 1 hour
    }
}
