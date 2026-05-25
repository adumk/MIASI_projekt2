package com.rental.infrastructure;

import com.rental.application.MarkOverdueUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueRentalScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueRentalScheduler.class);

    private final MarkOverdueUseCase markOverdueUseCase;

    public OverdueRentalScheduler(MarkOverdueUseCase markOverdueUseCase) {
        this.markOverdueUseCase = markOverdueUseCase;
    }

    @Scheduled(cron = "${rental.overdue.cron:0 0 1 * * *}")
    public void markOverdueRentals() {
        int count = markOverdueUseCase.handle();
        if (count > 0) {
            log.info("Marked {} rental(s) as OVERDUE", count);
        }
    }
}
