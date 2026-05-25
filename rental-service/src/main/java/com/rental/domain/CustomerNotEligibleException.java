package com.rental.domain;

public class CustomerNotEligibleException extends RuntimeException {

    public CustomerNotEligibleException(String message) {
        super(message);
    }
}
