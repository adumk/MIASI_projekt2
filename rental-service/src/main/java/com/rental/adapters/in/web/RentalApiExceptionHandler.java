package com.rental.adapters.in.web;

import com.rental.domain.CustomerNotEligibleException;
import com.rental.domain.InvalidPeriodException;
import com.rental.domain.InvalidStatusTransitionException;
import com.rental.domain.VehicleNotAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RentalApiExceptionHandler {

    @ExceptionHandler(VehicleNotAvailableException.class)
    public ProblemDetail handleNotAvailable(VehicleNotAvailableException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        detail.setTitle("Vehicle not available");
        return detail;
    }

    @ExceptionHandler({
            InvalidStatusTransitionException.class,
            InvalidPeriodException.class,
            CustomerNotEligibleException.class
    })
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid request");
        return detail;
    }
}
