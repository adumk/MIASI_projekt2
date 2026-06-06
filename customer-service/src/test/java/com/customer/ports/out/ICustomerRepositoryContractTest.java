package com.customer.ports.out;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.CustomerStatus;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ICustomerRepositoryContractTest {

    protected abstract ICustomerRepository getRepositoryInstance();

    private Customer activeCustomer(String id, String email) {
        Customer customer = Customer.create(
                CustomerId.of(id),
                PersonName.of("Jan", "Kowalski"),
                Email.of(email),
                DriverLicense.of("DL-" + id, LocalDate.now().plusYears(2)));
        customer.register();
        customer.clearDomainEvents();
        return customer;
    }

    @Test
    @DisplayName("Should save and find customer by ID")
    void shouldSaveAndFindCustomerById() {
        ICustomerRepository repository = getRepositoryInstance();

        Customer customer = activeCustomer("contract-cust-001", "jan001@example.com");
        repository.save(customer);

        Customer found = repository.findById(CustomerId.of("contract-cust-001"));

        assertThat(found).isNotNull();
        assertThat(found.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(found.getEmail().getValue()).isEqualTo("jan001@example.com");
    }

    @Test
    @DisplayName("Should return true when email is already registered")
    void shouldReturnTrueWhenEmailAlreadyRegistered() {
        ICustomerRepository repository = getRepositoryInstance();

        Customer customer = activeCustomer("contract-cust-002", "jan002@example.com");
        repository.save(customer);

        boolean exists = repository.existsByEmail(Email.of("jan002@example.com"));

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email is not registered")
    void shouldReturnFalseWhenEmailNotRegistered() {
        ICustomerRepository repository = getRepositoryInstance();

        boolean exists = repository.existsByEmail(Email.of("unknown@example.com"));

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find auth view by email when password is set")
    void shouldFindAuthViewByEmail() {
        ICustomerRepository repository = getRepositoryInstance();

        Customer customer = activeCustomer("contract-cust-003", "jan003@example.com");
        repository.saveWithPassword(customer, "hashed_password_xyz", "CUSTOMER");

        CustomerAuthView authView = repository.findAuthByEmail(Email.of("jan003@example.com"));

        assertThat(authView).isNotNull();
        assertThat(authView.customer()).isNotNull();
        assertThat(authView.passwordHash()).isEqualTo("hashed_password_xyz");
    }
}