//package com.fleet.adapters;
//
//import com.fleet.adapters.out.db.MongoVehicleRepositoryAdapter;
//import com.fleet.ports.out.IVehicleRepository;
//import com.fleet.ports.out.IVehicleRepositoryContractTest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.MongoDBContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@Testcontainers(disabledWithoutDocker = true)
//@SpringBootTest
//@ActiveProfiles("test")
//class MongoVehicleRepositoryTest extends IVehicleRepositoryContractTest {
//
//    @Container
//    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");
//
//    @DynamicPropertySource
//    static void registerMongoProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
//    }
//
//    @Autowired
//    private MongoVehicleRepositoryAdapter mongoAdapter;
//
//    @Override
//    protected IVehicleRepository getRepositoryInstance() {
//        return mongoAdapter;
//    }
//}