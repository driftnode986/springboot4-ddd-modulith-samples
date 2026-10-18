package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.OrderState.Returned;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultHidesReturnedTest {

    private static final Instant T0 =
            Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("default があると、扱っていない返品も「処理中」と表示される")
    void defaultがあると返品も処理中になる() {
        OrderState returned = new Returned(T0, "サイズ違い");

        assertThat(OrderStateView.label(returned))
                .isEqualTo("処理中");
    }
}
