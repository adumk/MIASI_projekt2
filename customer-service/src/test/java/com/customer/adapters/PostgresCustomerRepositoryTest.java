//package com.customer.adapters;
//
//import com.customer.adapters.out.db.PostgresCustomerRepositoryAdapter;
//import com.customer.ports.out.ICustomerRepository;
//import com.customer.ports.out.ICustomerRepositoryContractTest;
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
//class PostgresCustomerRepositoryTest extends ICustomerRepositoryContractTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
//            .withDatabaseName("test_customer_database")
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
//    private PostgresCustomerRepositoryAdapter databaseAdapter;
//
//    @Override
//    protected ICustomerRepository getRepositoryInstance() {
//        return databaseAdapter;
//    }
//}