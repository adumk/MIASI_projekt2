package com.fleet.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VehicleMongoRepository extends MongoRepository<VehicleDocument, String> {
}
