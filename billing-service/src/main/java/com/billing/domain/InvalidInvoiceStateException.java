package com.billing.domain;

public class InvalidInvoiceStateException extends RuntimeException {

    public InvalidInvoiceStateException(String message) {
        super(message);
    }
}
