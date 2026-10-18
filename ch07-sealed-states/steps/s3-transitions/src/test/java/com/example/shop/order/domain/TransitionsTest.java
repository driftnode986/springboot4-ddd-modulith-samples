package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.OrderState.Accepted;
import com.example.shop.order.domain.OrderState.Confirmed;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransitionsTest {

    private static final Instant T0 =
            Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("受付済から引当・支払いを経て確定に進み、事実を引き継ぐ")
    void 確定まで進み事実を引き継ぐ() {
        Confirmed confirmed = new Accepted(T0)
                .reserve(new ReservationId("R-1"))
                .pay(new PaymentId("PAY-1"))
                .confirm(T0.plusSeconds(60));

        assertThat(confirmed.acceptedAt()).isEqualTo(T0);
        assertThat(confirmed.reservationId())
                .isEqualTo(new ReservationId("R-1"));
        assertThat(confirmed.paymentId())
                .isEqualTo(new PaymentId("PAY-1"));
        assertThat(confirmed.confirmedAt())
                .isEqualTo(T0.plusSeconds(60));
    }
}
