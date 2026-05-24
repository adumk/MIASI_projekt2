package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Customer — aggregate behaviour")
class CustomerTest {

    private static final CustomerId CUSTOMER_ID = CustomerId.of(UUID.randomUUID());
    private static final LocalDate  VALID_LICENSE_EXPIRY = LocalDate.now().plusYears(2);
    private static final LocalDate  EXPIRED_LICENSE_EXPIRY = LocalDate.now().minusDays(1);

    // -------------------------------------------------------------------------
    // Creation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create an eligible customer in active status")
    void shouldCreateEligibleCustomer() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);

        assertThat(customer).isNotNull();
        assertThat(customer.id()).isEqualTo(CUSTOMER_ID);
        assertThat(customer.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    // -------------------------------------------------------------------------
    // canRent() rules
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should return true when customer is active and license is valid")
    void shouldReturnTrueWhenCustomerCanRentAndLicenseIsValid() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);

        assertThat(customer.canRent()).isTrue();
    }

    @Test
    @DisplayName("should return false when customer is blocked")
    void shouldReturnFalseWhenCustomerIsBlocked() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);
        customer.block();

        assertThat(customer.canRent()).isFalse();
    }

    @Test
    @DisplayName("should return false when customer is blacklisted")
    void shouldReturnFalseWhenCustomerIsBlacklisted() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);
        customer.blacklist();

        assertThat(customer.canRent()).isFalse();
    }

    @Test
    @DisplayName("should return false when driver license is expired")
    void shouldReturnFalseWhenDriverLicenseIsExpired() {
        Customer customer = Customer.create(CUSTOMER_ID, EXPIRED_LICENSE_EXPIRY);

        assertThat(customer.canRent()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Status transitions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should transition customer to VERIFIED status")
    void shouldTransitionToVerifiedStatus() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);
        customer.verify();

        assertThat(customer.status()).isEqualTo(CustomerStatus.VERIFIED);
    }

    @Test
    @DisplayName("should transition customer to BLOCKED status")
    void shouldTransitionToBlockedStatus() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);
        customer.block();

        assertThat(customer.status()).isEqualTo(CustomerStatus.BLOCKED);
    }

    // -------------------------------------------------------------------------
    // Domain events
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should emit CustomerRegistered event on creation")
    void shouldEmitCustomerRegisteredEvent() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);

        assertThat(customer.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CustomerRegistered.class);

        CustomerRegistered event = (CustomerRegistered) customer.domainEvents().get(0);
        assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    @DisplayName("should emit CustomerVerified event when customer is verified")
    void shouldEmitCustomerVerifiedEvent() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);
        customer.clearDomainEvents();
        customer.verify();

        assertThat(customer.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CustomerVerified.class);

        CustomerVerified event = (CustomerVerified) customer.domainEvents().get(0);
        assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    @DisplayName("should emit CustomerBlacklisted event when customer is blacklisted")
    void shouldEmitCustomerBlacklistedEvent() {
        Customer customer = Customer.create(CUSTOMER_ID, VALID_LICENSE_EXPIRY);
        customer.clearDomainEvents();
        customer.blacklist();

        assertThat(customer.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CustomerBlacklisted.class);

        CustomerBlacklisted event = (CustomerBlacklisted) customer.domainEvents().get(0);
        assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);
    }

    // -------------------------------------------------------------------------
    // Obvious invariant — reject null inputs
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should reject null customer id on creation")
    void shouldRejectNullCustomerId() {
        assertThatThrownBy(() -> Customer.create(null, VALID_LICENSE_EXPIRY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject null license expiry on creation")
    void shouldRejectNullLicenseExpiry() {
        assertThatThrownBy(() -> Customer.create(CUSTOMER_ID, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}