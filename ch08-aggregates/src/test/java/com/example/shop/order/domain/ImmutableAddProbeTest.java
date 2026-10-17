package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 取り出した一覧への追加が、コンパイルは通るのに実行時に拒まれることを固定する。
 * コンパイラが守れる範囲の境目を示すために置いている。
 */
class ImmutableAddProbeTest {

    private static final CustomerId CUSTOMER = new CustomerId("C-001");
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");

    // #region add-compiles
    @Test
    @DisplayName("取り出した一覧への追加はコンパイルできるが、"
            + "実行すると例外になる")
    void addingToReturnedListCompilesButFailsAtRuntime() {
        Order order =
                Order.place(new OrderId("O-200"), CUSTOMER, NOW);
        order.addLine(new ProductId("P-100"), new Quantity(1),
                Money.yen(980));

        // 次の行は型の上では正しいのでコンパイルが通る
        assertThatThrownBy(() -> order.lines().add(
                        new OrderLine(new ProductId("P-999"),
                                new Quantity(1), Money.yen(1))))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage(null);

        assertThat(order.lines()).hasSize(1);
    }
    // #endregion
}
