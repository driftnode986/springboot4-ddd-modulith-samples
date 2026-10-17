package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.OrderState.Accepted;
import com.example.shop.order.domain.OrderState.Paid;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatesTest {

    private static final Instant T0 = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("受付済は決済の識別子を持たない（別の型になる）")
    void 受付済は決済の識別子を持たない() {
        OrderState state = new Accepted(T0);

        assertThat(state).isNotInstanceOf(Paid.class);
        assertThat(state).isEqualTo(new Accepted(T0));
    }

    @Test
    @DisplayName("支払済は引当と決済の識別子を両方持つ")
    void 支払済は引当と決済の識別子を両方持つ() {
        Paid paid = new Paid(T0, new ReservationId("R-1"),
                new PaymentId("PAY-1"));

        assertThat(paid.reservationId()).isEqualTo(new ReservationId("R-1"));
        assertThat(paid.paymentId()).isEqualTo(new PaymentId("PAY-1"));
    }
}
