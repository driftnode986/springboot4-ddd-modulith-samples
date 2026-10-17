package com.example.shop.order.domain;

import java.time.Instant;

// #region states
public sealed interface OrderState {

    record Accepted(Instant acceptedAt)
            implements OrderState { }

    record Reserved(
            Instant acceptedAt,
            ReservationId reservationId)
            implements OrderState { }

    record Paid(
            Instant acceptedAt,
            ReservationId reservationId,
            PaymentId paymentId) implements OrderState { }

    record Confirmed(
            Instant acceptedAt,
            ReservationId reservationId,
            PaymentId paymentId,
            Instant confirmedAt) implements OrderState { }

    record Cancelled(Instant cancelledAt, String reason)
            implements OrderState { }
}
// #endregion
