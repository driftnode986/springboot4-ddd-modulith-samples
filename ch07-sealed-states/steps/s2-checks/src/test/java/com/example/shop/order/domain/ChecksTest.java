package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.order.domain.OrderState.Cancelled;
import com.example.shop.order.domain.OrderState.Paid;
import com.example.shop.order.domain.OrderState.Reserved;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChecksTest {

    private static final Instant T0 = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("引当の識別子を持たない引当済は作れない")
    void 引当の識別子を持たない引当済は作れない() {
        assertThatThrownBy(() -> new Reserved(T0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("決済の識別子・取消理由が欠けた状態は作れない")
    void 欠けた状態は作れない() {
        assertThatThrownBy(() -> new Paid(T0, new ReservationId("R-1"), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cancelled(T0, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
