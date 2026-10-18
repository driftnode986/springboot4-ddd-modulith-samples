package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.OrderState.Accepted;
import com.example.shop.order.domain.OrderState.Cancelled;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStateViewTest {

    private static final Instant T0 = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("支払済の表示には決済の識別子が入る")
    void 支払済の表示には決済の識別子が入る() {
        OrderState paid =
                new Accepted(T0).reserve(new ReservationId("R-1")).pay(new PaymentId("PAY-1"));

        assertThat(OrderStateView.label(paid)).isEqualTo("お支払いを確認しました (PAY-1)");
    }

    @Test
    @DisplayName("キャンセル済の表示には理由が入る")
    void キャンセル済の表示には理由が入る() {
        OrderState cancelled = new Cancelled(T0, "在庫を確保できなかったため");

        assertThat(OrderStateView.label(cancelled)).isEqualTo("キャンセル済み: 在庫を確保できなかったため");
    }
}
