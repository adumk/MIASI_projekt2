package com.rental.adapters;

import com.rental.ports.out.IRentalRepository;
import com.rental.ports.out.IRentalRepositoryContractTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class PostgresRentalRepositoryTest extends IRentalRepositoryContractTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_rental_database")
            .withUsername("qa_user")
            .withPassword("secure_qa_pass");

    @Autowired
    private PostgresRentalRepositoryAdapter databaseAdapter;

    @Override
    protected IRentalRepository getRepositoryInstance() {
        // Returns the production-ready adapter instance linked to the test container instance
        return databaseAdapter;
    }
}