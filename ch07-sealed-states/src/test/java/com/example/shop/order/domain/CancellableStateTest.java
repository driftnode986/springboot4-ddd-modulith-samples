package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.OrderState.Accepted;
import com.example.shop.order.domain.OrderState.Cancelled;
import com.example.shop.order.domain.OrderState.Confirmed;
import com.example.shop.order.domain.OrderState.Paid;
import com.example.shop.order.domain.OrderState.Reserved;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancellableStateTest {

    private static final Instant T0 = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("受付済はキャンセルできる")
    void 受付済はキャンセルできる() {
        CancellableState state = new Accepted(T0);

        Cancelled cancelled = state.cancel(T0.plusSeconds(60), "顧客都合");

        assertThat(cancelled.reason()).isEqualTo("顧客都合");
    }

    @Test
    @DisplayName("支払済もキャンセルできる")
    void 支払済もキャンセルできる() {
        Paid paid = new Accepted(T0).reserve(new ReservationId("R-1")).pay(new PaymentId("PAY-1"));

        Cancelled cancelled = paid.cancel(T0.plusSeconds(60), "顧客都合");

        assertThat(cancelled.reason()).isEqualTo("顧客都合");
    }

    @Test
    @DisplayName("確定済はキャンセルできる状態として扱えない")
    void 確定済はキャンセルできる状態として扱えない() {
        Confirmed confirmed =
                new Accepted(T0)
                        .reserve(new ReservationId("R-1"))
                        .pay(new PaymentId("PAY-1"))
                        .confirm(T0.plusSeconds(60));

        assertThat(confirmed).isNotInstanceOf(CancellableState.class);
    }

    @Test
    @DisplayName("引当済はキャンセルできる状態として扱える")
    void 引当済はキャンセルできる状態として扱える() {
        Reserved reserved = new Accepted(T0).reserve(new ReservationId("R-1"));

        assertThat(reserved).isInstanceOf(CancellableState.class);
    }
}
