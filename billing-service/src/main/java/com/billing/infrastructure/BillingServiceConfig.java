package com.billing.infrastructure;

import com.billing.application.ApplyDamageFeeUseCase;
import com.billing.application.CalculateCostUseCase;
import com.billing.application.GenerateInvoiceUseCase;
import com.billing.application.GetOrPrepareInvoiceUseCase;
import com.billing.application.ProcessPaymentUseCase;
import com.billing.ports.out.IDamageFeeStore;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import com.billing.ports.out.IPaymentRepository;
import com.billing.ports.out.IRentalSnapshotPort;
import com.billing.ports.out.IVehicleCategoryResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingServiceConfig {

    @Bean
    CalculateCostUseCase calculateCostUseCase(
            IInvoiceRepository invoiceRepository,
            IDomainEventPublisher domainEventPublisher,
            ICostCalculatedEventPublisher costCalculatedEventPublisher,
            IDamageFeeStore damageFeeStore) {
        return new CalculateCostUseCase(
                invoiceRepository, domainEventPublisher, costCalculatedEventPublisher, damageFeeStore);
    }

    @Bean
    ApplyDamageFeeUseCase applyDamageFeeUseCase(IDamageFeeStore damageFeeStore) {
        return new ApplyDamageFeeUseCase(damageFeeStore);
    }

    @Bean
    GenerateInvoiceUseCase generateInvoiceUseCase(
            IInvoiceRepository invoiceRepository,
            IDomainEventPublisher domainEventPublisher) {
        return new GenerateInvoiceUseCase(invoiceRepository, domainEventPublisher);
    }

    @Bean
    ProcessPaymentUseCase processPaymentUseCase(
            IInvoiceRepository invoiceRepository,
            IPaymentRepository paymentRepository,
            IDomainEventPublisher domainEventPublisher) {
        return new ProcessPaymentUseCase(invoiceRepository, paymentRepository, domainEventPublisher);
    }

    @Bean
    GetOrPrepareInvoiceUseCase getOrPrepareInvoiceUseCase(
            IInvoiceRepository invoiceRepository,
            IRentalSnapshotPort rentalSnapshotPort,
            IVehicleCategoryResolver vehicleCategoryResolver,
            CalculateCostUseCase calculateCostUseCase,
            GenerateInvoiceUseCase generateInvoiceUseCase) {
        return new GetOrPrepareInvoiceUseCase(
                invoiceRepository,
                rentalSnapshotPort,
                vehicleCategoryResolver,
                calculateCostUseCase,
                generateInvoiceUseCase);
    }
}
