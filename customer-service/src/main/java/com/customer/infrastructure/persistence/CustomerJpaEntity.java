package com.customer.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class CustomerJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(name = "license_expiry", nullable = false)
    private LocalDate licenseExpiry;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "block_reason")
    private String blockReason;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role;

    protected CustomerJpaEntity() {
    }

    public CustomerJpaEntity(
            String customerId,
            String firstName,
            String lastName,
            String email,
            String licenseNumber,
            LocalDate licenseExpiry,
            String status,
            boolean verified,
            String blockReason,
            String passwordHash,
            String role) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.licenseExpiry = licenseExpiry;
        this.status = status;
        this.verified = verified;
        this.blockReason = blockReason;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : "CUSTOMER";
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public LocalDate getLicenseExpiry() {
        return licenseExpiry;
    }

    public String getStatus() {
        return status;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
