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

class OrderStateTest {

    private static final Instant T0 = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("受付済からは引当済に進める")
    void 受付済からは引当済に進める() {
        Accepted accepted = new Accepted(T0);

        Reserved reserved = accepted.reserve(new ReservationId("R-1"));

        assertThat(reserved.reservationId()).isEqualTo(new ReservationId("R-1"));
    }

    @Test
    @DisplayName("引当済からは支払済に進める")
    void 引当済からは支払済に進める() {
        Reserved reserved = new Accepted(T0).reserve(new ReservationId("R-1"));

        Paid paid = reserved.pay(new PaymentId("PAY-1"));

        assertThat(paid.paymentId()).isEqualTo(new PaymentId("PAY-1"));
        assertThat(paid.reservationId()).isEqualTo(new ReservationId("R-1"));
    }

    @Test
    @DisplayName("支払済からは確定に進める")
    void 支払済からは確定に進める() {
        Paid paid = new Accepted(T0).reserve(new ReservationId("R-1")).pay(new PaymentId("PAY-1"));

        Confirmed confirmed = paid.confirm(T0.plusSeconds(60));

        assertThat(confirmed.paymentId()).isEqualTo(new PaymentId("PAY-1"));
    }

    @Test
    @DisplayName("受付済は決済の識別子を持たない")
    void 受付済は決済の識別子を持たない() {
        OrderState state = new Accepted(T0);

        assertThat(state).isInstanceOf(Accepted.class);
        assertThat(state).isNotInstanceOf(Paid.class);
    }

    @Test
    @DisplayName("遷移しても元の状態は変わらない")
    void 遷移しても元の状態は変わらない() {
        Accepted accepted = new Accepted(T0);

        accepted.reserve(new ReservationId("R-1"));

        assertThat(accepted).isEqualTo(new Accepted(T0));
    }

    @Test
    @DisplayName("取り消した状態は理由を持つ")
    void 取り消した状態は理由を持つ() {
        Cancelled cancelled = new Cancelled(T0, "在庫を確保できなかったため");

        assertThat(cancelled.reason()).isEqualTo("在庫を確保できなかったため");
    }
}
