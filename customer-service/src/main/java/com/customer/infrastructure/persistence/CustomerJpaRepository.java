package com.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, String> {

    boolean existsByEmail(String email);

    Optional<CustomerJpaEntity> findByEmail(String email);
}
