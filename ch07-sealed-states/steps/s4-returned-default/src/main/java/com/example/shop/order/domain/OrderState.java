package com.example.shop.order.domain;

import java.time.Instant;

public sealed interface OrderState {

    record Accepted(Instant acceptedAt) implements OrderState {

        public Accepted {
            if (acceptedAt == null) {
                throw new IllegalArgumentException(
                        "受付日時は必須です");
            }
        }

        public Reserved reserve(ReservationId reservationId) {
            return new Reserved(acceptedAt, reservationId);
        }
    }

    record Reserved(
            Instant acceptedAt,
            ReservationId reservationId) implements OrderState {

        public Reserved {
            if (reservationId == null) {
                throw new IllegalArgumentException(
                        "引当の識別子は必須です");
            }
        }

        public Paid pay(PaymentId paymentId) {
            return new Paid(acceptedAt, reservationId,
                    paymentId);
        }
    }

    record Paid(
            Instant acceptedAt,
            ReservationId reservationId,
            PaymentId paymentId) implements OrderState {

        public Paid {
            if (paymentId == null) {
                throw new IllegalArgumentException(
                        "決済の識別子は必須です");
            }
        }

        public Confirmed confirm(Instant confirmedAt) {
            return new Confirmed(acceptedAt, reservationId,
                    paymentId, confirmedAt);
        }
    }

    record Confirmed(
            Instant acceptedAt,
            ReservationId reservationId,
            PaymentId paymentId,
            Instant confirmedAt) implements OrderState { }

    record Cancelled(Instant cancelledAt, String reason)
            implements OrderState {

        public Cancelled {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(
                        "取消理由は必須です");
            }
        }
    }

    record Returned(Instant returnedAt, String reason)
            implements OrderState { }
}
