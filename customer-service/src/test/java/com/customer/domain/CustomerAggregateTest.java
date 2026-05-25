package com.customer.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Customer aggregate — lifecycle and domain event emission")
class CustomerAggregateTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate IN_ONE_YEAR = TODAY.plusYears(1);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);

    private CustomerId customerId;
    private PersonName fullName;
    private Email email;
    private DriverLicense validLicense;

    @BeforeEach
    void setUp() {
        customerId = CustomerId.of("customer-001");
        fullName = PersonName.of("Jan", "Kowalski");
        email = Email.of("jan.kowalski@example.com");
        validLicense = DriverLicense.of("DL-12345", IN_ONE_YEAR);
    }

    private Customer newCustomer() {
        return Customer.create(customerId, fullName, email, validLicense);
    }

    @Nested
    @DisplayName("register() — customer onboarding")
    class Register {

        @Test
        @DisplayName("Should emit CustomerRegistered event when customer registers")
        void shouldRegisterCustomerAndEmitEvent() {
            Customer customer = newCustomer();

            customer.register();

            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(CustomerRegistered.class);

            CustomerRegistered event = (CustomerRegistered) customer.getDomainEvents().get(0);
            assertThat(event.getCustomerId()).isEqualTo(customerId);
            assertThat(event.getEmail()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("verify() — identity verification")
    class Verify {

        @Test
        @DisplayName("Should mark customer as verified and emit CustomerVerified event")
        void shouldVerifyActiveCustomer() {
            Customer customer = newCustomer();
            customer.register();

            customer.verify();

            assertThat(customer.isVerified()).isTrue();
            assertThat(customer.getDomainEvents())
                    .filteredOn(e -> e instanceof CustomerVerified)
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should throw InvalidStatusTransitionException when verifying twice")
        void shouldRejectDoubleVerification() {
            Customer customer = newCustomer();
            customer.register();
            customer.verify();

            assertThatThrownBy(() -> customer.verify())
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("block() — account suspension")
    class Block {

        @Test
        @DisplayName("Should transition to BLOCKED with reason")
        void shouldBlockActiveCustomer() {
            Customer customer = newCustomer();
            customer.register();

            customer.block("unpaid invoices");

            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.BLOCKED);
            assertThat(customer.getBlockReason()).isEqualTo("unpaid invoices");
        }

        @Test
        @DisplayName("Should throw InvalidStatusTransitionException when blocking non-ACTIVE customer")
        void shouldRejectBlockWhenNotActive() {
            Customer customer = newCustomer();
            customer.register();
            customer.block("fraud suspicion");

            assertThatThrownBy(() -> customer.block("repeat offence"))
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("canRent() — rental eligibility ACL")
    class CanRent {

        @Test
        @DisplayName("Should return true when ACTIVE, verified and license is valid")
        void shouldAllowRentForEligibleCustomer() {
            Customer customer = newCustomer();
            customer.register();
            customer.verify();

            assertThat(customer.canRent()).isTrue();
        }

        @Test
        @DisplayName("Should return false when customer is not verified")
        void shouldDenyRentWhenNotVerified() {
            Customer customer = newCustomer();
            customer.register();

            assertThat(customer.canRent()).isFalse();
        }

        @Test
        @DisplayName("Should return false when driver license has expired")
        void shouldDenyRentWhenLicenseExpired() {
            DriverLicense expiredLicense = DriverLicense.of("DL-99999", YESTERDAY);
            Customer customer = Customer.create(customerId, fullName, email, expiredLicense);
            customer.register();
            customer.verify();

            assertThat(customer.canRent()).isFalse();
        }

        @Test
        @DisplayName("Should return false when customer is BLOCKED")
        void shouldDenyRentWhenBlocked() {
            Customer customer = newCustomer();
            customer.register();
            customer.verify();
            customer.block("policy violation");

            assertThat(customer.canRent()).isFalse();
        }
    }
}
