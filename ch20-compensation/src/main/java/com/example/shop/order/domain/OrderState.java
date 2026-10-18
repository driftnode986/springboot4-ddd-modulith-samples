package com.example.shop.order.domain;

import java.time.Instant;

public sealed interface OrderState {

    // #region accepted-head
    record Accepted(Instant acceptedAt)
            implements OrderState, CancellableState {
    // #endregion

        public Accepted {
            if (acceptedAt == null) {
                throw new IllegalArgumentException(
                        "受付日時は必須です");
            }
        }

        public Reserved reserve(ReservationId reservationId) {
            return new Reserved(acceptedAt, reservationId);
        }

        // #region accepted-cancel
        @Override
        public Cancelled cancel(Instant cancelledAt,
                String reason) {
            return new Cancelled(cancelledAt, reason);
        }
    }
    // #endregion

    record Reserved(
            Instant acceptedAt,
            ReservationId reservationId)
            implements OrderState, CancellableState {

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

        @Override
        public Cancelled cancel(Instant cancelledAt,
                String reason) {
            return new Cancelled(cancelledAt, reason);
        }
    }

    record Paid(
            Instant acceptedAt,
            ReservationId reservationId,
            PaymentId paymentId)
            implements OrderState, CancellableState {

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

        @Override
        public Cancelled cancel(Instant cancelledAt,
                String reason) {
            return new Cancelled(cancelledAt, reason);
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
}
