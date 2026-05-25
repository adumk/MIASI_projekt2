package com.customer.adapters.out.db;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.CustomerStatus;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import com.customer.infrastructure.persistence.CustomerJpaEntity;
import com.customer.infrastructure.persistence.CustomerJpaRepository;
import com.customer.ports.out.CustomerAuthView;
import com.customer.ports.out.ICustomerRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresCustomerRepositoryAdapter implements ICustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    public PostgresCustomerRepositoryAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Customer customer) {
        String passwordHash = null;
        String role = "CUSTOMER";
        var existing = jpaRepository.findById(customer.getCustomerId().getValue());
        if (existing.isPresent()) {
            passwordHash = existing.get().getPasswordHash();
            role = existing.get().getRole();
        }
        jpaRepository.save(toEntity(customer, passwordHash, role));
    }

    @Override
    public void saveWithPassword(Customer customer, String passwordHash, String role) {
        jpaRepository.save(toEntity(customer, passwordHash, role));
    }

    @Override
    public Customer findById(CustomerId customerId) {
        return jpaRepository.findById(customerId.getValue()).map(this::toDomain).orElse(null);
    }

    @Override
    public Customer findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue()).map(this::toDomain).orElse(null);
    }

    @Override
    public CustomerAuthView findAuthByEmail(Email email) {
        return jpaRepository
                .findByEmail(email.getValue())
                .map(e -> new CustomerAuthView(toDomain(e), e.getPasswordHash(), e.getRole()))
                .orElse(null);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    private CustomerJpaEntity toEntity(Customer customer, String passwordHash, String role) {
        return new CustomerJpaEntity(
                customer.getCustomerId().getValue(),
                customer.getFullName().getFirstName(),
                customer.getFullName().getLastName(),
                customer.getEmail().getValue(),
                customer.getDriverLicense().getNumber(),
                customer.getDriverLicense().getExpiryDate(),
                customer.getStatus().name(),
                customer.isVerified(),
                customer.getBlockReason(),
                passwordHash,
                role);
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        return Customer.reconstitute(
                CustomerId.of(entity.getCustomerId()),
                PersonName.of(entity.getFirstName(), entity.getLastName()),
                Email.of(entity.getEmail()),
                DriverLicense.of(entity.getLicenseNumber(), entity.getLicenseExpiry()),
                CustomerStatus.valueOf(entity.getStatus()),
                entity.isVerified(),
                entity.getBlockReason());
    }
}
