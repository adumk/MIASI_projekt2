package com.rental.adapters;

import com.rental.adapters.out.db.PostgresRentalRepositoryAdapter;
import com.rental.ports.out.IRentalRepository;
import com.rental.ports.out.IRentalRepositoryContractTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class PostgresRentalRepositoryTest extends IRentalRepositoryContractTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_rental_database")
            .withUsername("qa_user")
            .withPassword("secure_qa_pass");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private PostgresRentalRepositoryAdapter databaseAdapter;

    @Override
    protected IRentalRepository getRepositoryInstance() {
        // Returns the production-ready adapter instance linked to the test container instance
        return databaseAdapter;
    }
}