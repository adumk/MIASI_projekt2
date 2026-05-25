CREATE TABLE customers (
    customer_id      VARCHAR(64)  PRIMARY KEY,
    first_name       VARCHAR(128) NOT NULL,
    last_name        VARCHAR(128) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    license_number   VARCHAR(64)  NOT NULL,
    license_expiry   DATE         NOT NULL,
    status           VARCHAR(32)  NOT NULL,
    verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    block_reason     VARCHAR(512)
);
