CREATE TABLE invoices (
    invoice_id        VARCHAR(64)  PRIMARY KEY,
    rental_id         VARCHAR(64)  NOT NULL UNIQUE,
    customer_id       VARCHAR(64)  NOT NULL,
    amount            BIGINT       NOT NULL,
    currency          VARCHAR(3)   NOT NULL,
    rental_days       INT          NOT NULL,
    vehicle_category  VARCHAR(32)  NOT NULL,
    status            VARCHAR(32)  NOT NULL
);

CREATE TABLE payments (
    payment_id  VARCHAR(64) PRIMARY KEY,
    invoice_id  VARCHAR(64) NOT NULL,
    rental_id   VARCHAR(64) NOT NULL,
    amount      BIGINT      NOT NULL,
    currency    VARCHAR(3)  NOT NULL,
    status      VARCHAR(32) NOT NULL,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id)
);

CREATE INDEX idx_payments_rental_id ON payments (rental_id);
