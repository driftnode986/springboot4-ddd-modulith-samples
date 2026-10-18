package com.example.shop.order.domain;

import java.time.Instant;

/**
 * 注文の状態。取りうる状態はこのファイルに列挙したものだけで、
 * 状態ごとに持つ項目が違う。
 */
public sealed interface OrderState {

    /** 受け付けただけの状態。在庫も支払いもまだ動いていない。 */
    record Accepted(Instant acceptedAt) implements OrderState, CancellableState {
        public Accepted {
            if (acceptedAt == null) {
                throw new IllegalArgumentException("受付日時は必須です");
            }
        }

        public Reserved reserve(ReservationId reservationId) {
            return new Reserved(acceptedAt, reservationId);
        }

        @Override
        public Cancelled cancel(Instant cancelledAt, String reason) {
            return new Cancelled(cancelledAt, reason);
        }
    }

    /** 在庫を引き当てた状態。引当の識別子を持つ。 */
    record Reserved(Instant acceptedAt, ReservationId reservationId)
            implements OrderState, CancellableState {
        public Reserved {
            if (reservationId == null) {
                throw new IllegalArgumentException("引当の識別子は必須です");
            }
        }

        public Paid pay(PaymentId paymentId) {
            return new Paid(acceptedAt, reservationId, paymentId);
        }

        @Override
        public Cancelled cancel(Instant cancelledAt, String reason) {
            return new Cancelled(cancelledAt, reason);
        }
    }

    /** 支払いが済んだ状態。決済の識別子を持つ。 */
    record Paid(Instant acceptedAt, ReservationId reservationId, PaymentId paymentId)
            implements OrderState, CancellableState {
        public Paid {
            if (paymentId == null) {
                throw new IllegalArgumentException("決済の識別子は必須です");
            }
        }

        public Confirmed confirm(Instant confirmedAt) {
            return new Confirmed(acceptedAt, reservationId, paymentId, confirmedAt);
        }

        @Override
        public Cancelled cancel(Instant cancelledAt, String reason) {
            return new Cancelled(cancelledAt, reason);
        }
    }

    /** 出荷が確定した状態。ここから先には進まない。 */
    record Confirmed(
            Instant acceptedAt,
            ReservationId reservationId,
            PaymentId paymentId,
            Instant confirmedAt)
            implements OrderState {}

    /** 取り消された状態。どの段階から取り消したかを残す。 */
    record Cancelled(Instant cancelledAt, String reason) implements OrderState {
        public Cancelled {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("取消理由は必須です");
            }
        }
    }
}
