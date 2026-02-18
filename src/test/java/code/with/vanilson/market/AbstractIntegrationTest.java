//package code.with.vanilson.market;
//
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.test.context.ActiveProfiles;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.utility.DockerImageName;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
//        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
//})
//@ActiveProfiles("test")
//@AutoConfigureMockMvc
//@SuppressWarnings("all")
//public abstract class AbstractIntegrationTest {
//
//    @ServiceConnection
//    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
//            DockerImageName.parse("postgres:15-alpine"))
//            .withDatabaseName("market_db")
//            .withUsername("postgres")
//            .withPassword("postgres")
//            .waitingFor(Wait.forListeningPort());
//
//    @ServiceConnection
//    @Container
//    static com.redis.testcontainers.RedisContainer redis = new com.redis.testcontainers.RedisContainer(
//            DockerImageName.parse("redis:7-alpine"));
//
//    static {
//        postgres.start();
//        redis.start();
//    }
//
//    @MockBean
//    protected org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
//
//}
