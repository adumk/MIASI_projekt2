CREATE TABLE rentals (
    rental_id   VARCHAR(64)  PRIMARY KEY,
    vehicle_id  VARCHAR(64)  NOT NULL,
    customer_id VARCHAR(64)  NOT NULL,
    period_start DATE        NOT NULL,
    period_end   DATE        NOT NULL,
    status       VARCHAR(32)  NOT NULL
);
