package com.billing.adapters.in.web;

public record InvoiceResponse(
        String invoiceId,
        String rentalId,
        String customerId,
        long amount,
        String currency,
        int rentalDays,
        String vehicleCategory,
        String status) {
}
