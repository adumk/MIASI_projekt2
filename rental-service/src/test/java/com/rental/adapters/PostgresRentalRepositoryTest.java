package com.rental.adapters;

import com.rental.ports.out.IRentalRepository;
import com.rental.ports.out.IRentalRepositoryContractTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test that verifies the full IRentalRepository contract
 * against a real PostgreSQL database spun up via Testcontainers.
 *
 * All contract scenarios are inherited from {@link IRentalRepositoryContractTest}.
 * No mocks are used — this test exercises the real adapter end-to-end.
 */
@Testcontainers
@SpringBootTest
class PostgresRentalRepositoryTest extends IRentalRepositoryContractTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rental_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostgresRentalRepositoryAdapter adapter;

    @BeforeEach
    void cleanDatabase() {
        adapter.deleteAll();
    }

    @Override
    protected IRentalRepository repository() {
        return adapter;
    }
}