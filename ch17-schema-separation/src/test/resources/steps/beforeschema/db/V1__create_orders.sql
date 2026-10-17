CREATE TABLE orders (
    id              VARCHAR(36)     NOT NULL,
    customer_id     VARCHAR(36)     NOT NULL,
    state           VARCHAR(20)     NOT NULL,
    accepted_at     TIMESTAMPTZ,
    reservation_id  VARCHAR(36),
    payment_id      VARCHAR(36),
    confirmed_at    TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ,
    cancel_reason   VARCHAR(200),
    version         BIGINT          NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE order_lines (
    id            BIGSERIAL      NOT NULL,
    order_id      VARCHAR(36)    NOT NULL,
    product_id    VARCHAR(36)    NOT NULL,
    quantity      INTEGER        NOT NULL,
    unit_amount   NUMERIC(12, 2) NOT NULL,
    unit_currency VARCHAR(3)     NOT NULL,
    CONSTRAINT pk_order_lines PRIMARY KEY (id),
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
