//package com.billing.adapters;
//
//import com.billing.adapters.out.db.PostgresPaymentRepositoryAdapter;
//import com.billing.ports.out.IPaymentRepository;
//import com.billing.ports.out.IPaymentRepositoryContractTest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers(disabledWithoutDocker = true)
//@SpringBootTest
//class PostgresPaymentRepositoryTest extends IPaymentRepositoryContractTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
//            .withDatabaseName("test_billing_database")
//            .withUsername("qa_user")
//            .withPassword("secure_qa_pass");
//
//    @DynamicPropertySource
//    static void registerDataSource(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgresContainer::getUsername);
//        registry.add("spring.datasource.password", postgresContainer::getPassword);
//    }
//
//    @Autowired
//    private PostgresPaymentRepositoryAdapter databaseAdapter;
//
//    @Override
//    protected IPaymentRepository getRepositoryInstance() {
//        return databaseAdapter;
//    }
//}