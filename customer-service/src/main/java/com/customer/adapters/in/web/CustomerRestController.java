package com.customer.adapters.in.web;

import com.customer.application.BlockCustomerCommand;
import com.customer.application.BlockCustomerUseCase;
import com.customer.application.GetCustomerQuery;
import com.customer.application.GetCustomerUseCase;
import com.customer.application.RegisterCustomerCommand;
import com.customer.application.RegisterCustomerUseCase;
import com.customer.application.VerifyCustomerCommand;
import com.customer.application.VerifyCustomerUseCase;
import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerRestController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final VerifyCustomerUseCase verifyCustomerUseCase;
    private final BlockCustomerUseCase blockCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;

    public CustomerRestController(
            RegisterCustomerUseCase registerCustomerUseCase,
            VerifyCustomerUseCase verifyCustomerUseCase,
            BlockCustomerUseCase blockCustomerUseCase,
            GetCustomerUseCase getCustomerUseCase) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.verifyCustomerUseCase = verifyCustomerUseCase;
        this.blockCustomerUseCase = blockCustomerUseCase;
        this.getCustomerUseCase = getCustomerUseCase;
    }

    @PostMapping
    public ResponseEntity<RegisterCustomerResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                PersonName.of(request.firstName(), request.lastName()),
                Email.of(request.email()),
                DriverLicense.of(request.licenseNumber(), request.licenseExpiryDate()));
        CustomerId customerId = registerCustomerUseCase.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterCustomerResponse(customerId.getValue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable("id") String id) {
        Customer customer = getCustomerUseCase.handle(new GetCustomerQuery(CustomerId.of(id)));
        return ResponseEntity.ok(CustomerResponse.from(customer));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Void> verify(@PathVariable("id") String id) {
        verifyCustomerUseCase.handle(new VerifyCustomerCommand(CustomerId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> block(
            @PathVariable("id") String id, @Valid @RequestBody BlockCustomerRequest request) {
        blockCustomerUseCase.handle(new BlockCustomerCommand(CustomerId.of(id), request.reason()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/can-rent")
    public ResponseEntity<Boolean> canRent(@PathVariable("id") String id) {
        Customer customer = getCustomerUseCase.handle(new GetCustomerQuery(CustomerId.of(id)));
        return ResponseEntity.ok(customer.canRent());
    }
}
