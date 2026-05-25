package com.rental.adapters.in.web;

import com.rental.application.CancelReservationCommand;
import com.rental.application.CancelReservationUseCase;
import com.rental.application.ConfirmReservationCommand;
import com.rental.application.ConfirmReservationUseCase;
import com.rental.application.CreateReservationCommand;
import com.rental.application.CreateReservationUseCase;
import com.rental.application.GetRentalHistoryQuery;
import com.rental.application.GetRentalHistoryUseCase;
import com.rental.application.RentVehicleCommand;
import com.rental.application.RentVehicleUseCase;
import com.rental.application.ReturnVehicleCommand;
import com.rental.application.ReturnVehicleUseCase;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.VehicleId;
import com.rental.ports.out.IRentalRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.rental.domain.RentalStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RentalRestController {

    private final CreateReservationUseCase createReservationUseCase;
    private final RentVehicleUseCase rentVehicleUseCase;
    private final ReturnVehicleUseCase returnVehicleUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final GetRentalHistoryUseCase getRentalHistoryUseCase;
    private final IRentalRepository rentalRepository;

    public RentalRestController(
            CreateReservationUseCase createReservationUseCase,
            RentVehicleUseCase rentVehicleUseCase,
            ReturnVehicleUseCase returnVehicleUseCase,
            CancelReservationUseCase cancelReservationUseCase,
            ConfirmReservationUseCase confirmReservationUseCase,
            GetRentalHistoryUseCase getRentalHistoryUseCase,
            IRentalRepository rentalRepository) {
        this.createReservationUseCase = createReservationUseCase;
        this.rentVehicleUseCase = rentVehicleUseCase;
        this.returnVehicleUseCase = returnVehicleUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
        this.confirmReservationUseCase = confirmReservationUseCase;
        this.getRentalHistoryUseCase = getRentalHistoryUseCase;
        this.rentalRepository = rentalRepository;
    }

    @PostMapping("/reservations")
    public ResponseEntity<RentalResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Rental rental = createReservationUseCase.handle(new CreateReservationCommand(
                CustomerId.of(request.customerId()),
                VehicleId.of(request.vehicleId()),
                DateRange.of(request.startDate(), request.endDate())));
        return ResponseEntity.status(HttpStatus.CREATED).body(RentalResponse.from(rental));
    }

    @PostMapping("/reservations/{rentalId}/confirm")
    public ResponseEntity<RentalResponse> confirmReservation(@PathVariable("rentalId") String rentalId) {
        confirmReservationUseCase.handle(new ConfirmReservationCommand(RentalId.of(rentalId)));
        Rental rental = rentalRepository.findById(RentalId.of(rentalId));
        return ResponseEntity.ok(RentalResponse.from(rental));
    }

    @PostMapping("/rentals/{rentalId}/activate")
    public ResponseEntity<RentalResponse> activateRental(@PathVariable("rentalId") String rentalId) {
        rentVehicleUseCase.handle(new RentVehicleCommand(RentalId.of(rentalId)));
        Rental rental = rentalRepository.findById(RentalId.of(rentalId));
        return ResponseEntity.ok(RentalResponse.from(rental));
    }

    @PostMapping("/rentals/{rentalId}/return")
    public ResponseEntity<RentalResponse> returnRental(
            @PathVariable("rentalId") String rentalId,
            @Valid @RequestBody ReturnVehicleRequest request) {
        returnVehicleUseCase.handle(new ReturnVehicleCommand(
                RentalId.of(rentalId),
                request.actualReturnDate(),
                request.mileage(),
                request.inspectionNotes()));
        Rental rental = rentalRepository.findById(RentalId.of(rentalId));
        return ResponseEntity.ok(RentalResponse.from(rental));
    }

    @PostMapping("/reservations/{rentalId}/cancel")
    public ResponseEntity<RentalResponse> cancelReservation(@PathVariable("rentalId") String rentalId) {
        cancelReservationUseCase.handle(new CancelReservationCommand(RentalId.of(rentalId)));
        Rental rental = rentalRepository.findById(RentalId.of(rentalId));
        return ResponseEntity.ok(RentalResponse.from(rental));
    }

    @GetMapping("/rentals")
    public ResponseEntity<List<RentalResponse>> listRentals(
            @RequestParam(required = false) RentalStatus status) {
        List<Rental> rentals = status != null
                ? rentalRepository.findByStatus(status)
                : rentalRepository.findAll();
        return ResponseEntity.ok(rentals.stream().map(RentalResponse::from).toList());
    }

    @GetMapping("/rentals/{rentalId}")
    public ResponseEntity<RentalResponse> getRental(@PathVariable("rentalId") String rentalId) {
        Rental rental = rentalRepository.findById(RentalId.of(rentalId));
        if (rental == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(RentalResponse.from(rental));
    }

    @GetMapping("/customers/{customerId}/rentals")
    public ResponseEntity<List<RentalResponse>> getRentalHistory(@PathVariable("customerId") String customerId) {
        List<RentalResponse> history = getRentalHistoryUseCase.handle(new GetRentalHistoryQuery(CustomerId.of(customerId)))
                .stream()
                .map(RentalResponse::from)
                .toList();
        return ResponseEntity.ok(history);
    }
}
