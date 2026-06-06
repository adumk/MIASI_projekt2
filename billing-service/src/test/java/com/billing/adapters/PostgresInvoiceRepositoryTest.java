//package com.billing.adapters;
//
//import com.billing.adapters.out.db.PostgresInvoiceRepositoryAdapter;
//import com.billing.ports.out.IInvoiceRepository;
//import com.billing.ports.out.IInvoiceRepositoryContractTest;
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
//class PostgresInvoiceRepositoryTest extends IInvoiceRepositoryContractTest {
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
//    private PostgresInvoiceRepositoryAdapter databaseAdapter;
//
//    @Override
//    protected IInvoiceRepository getRepositoryInstance() {
//        return databaseAdapter;
//    }
//}