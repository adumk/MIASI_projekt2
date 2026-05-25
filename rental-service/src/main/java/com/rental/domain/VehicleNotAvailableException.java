package com.rental.domain;

public class VehicleNotAvailableException extends RuntimeException {

    public VehicleNotAvailableException(String message) {
        super(message);
    }
}
