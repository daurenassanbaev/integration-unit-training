package net.proselyte.qafordevs.it;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractRestControllerBaseTest {
    @Container
    static final PostgreSQLContainer POSTGRESQL_CONTAINER;
    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer("postgres:latest")
                .withUsername("postgres")
                .withPassword("postgres")
                .withDatabaseName("qafordevs_testcontainers");
        POSTGRESQL_CONTAINER.start();
    }
    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);

    }
}
