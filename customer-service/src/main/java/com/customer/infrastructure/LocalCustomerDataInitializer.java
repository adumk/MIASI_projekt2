package com.customer.infrastructure;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.CustomerStatus;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import com.customer.ports.out.ICustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Profile("local")
public class LocalCustomerDataInitializer {

    @Bean
    CommandLineRunner seedCustomers(ICustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seed(
                    customerRepository,
                    passwordEncoder,
                    "customer-001",
                    "Jan",
                    "Kowalski",
                    "jan.kowalski@example.com",
                    "CUSTOMER",
                    true);
            seed(
                    customerRepository,
                    passwordEncoder,
                    "customer-admin",
                    "Admin",
                    "System",
                    "admin@wypozyczalnia.pl",
                    "ADMIN",
                    true);
            seed(
                    customerRepository,
                    passwordEncoder,
                    "employee-001",
                    "Anna",
                    "Pracownik",
                    "pracownik@wypozyczalnia.pl",
                    "EMPLOYEE",
                    true);
        };
    }

    private static void seed(
            ICustomerRepository repo,
            PasswordEncoder encoder,
            String id,
            String first,
            String last,
            String email,
            String role,
            boolean verified) {
        CustomerId customerId = CustomerId.of(id);
        Email emailVo = Email.of(email);
        Customer existing = repo.findById(customerId);
        if (existing == null) {
            Customer customer = Customer.reconstitute(
                    customerId,
                    PersonName.of(first, last),
                    emailVo,
                    DriverLicense.of("ABC123456", LocalDate.now().plusYears(5)),
                    CustomerStatus.ACTIVE,
                    verified,
                    null);
            repo.saveWithPassword(customer, encoder.encode("Haslo123!"), role);
            return;
        }
        var auth = repo.findAuthByEmail(emailVo);
        if (auth != null && (auth.passwordHash() == null || auth.passwordHash().isBlank())) {
            repo.saveWithPassword(existing, encoder.encode("Haslo123!"), role);
        }
    }
}
