package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 集約の境界をどこに引いたかを、後から崩されないように書き留めておくテスト。
 * 実装の正しさではなく設計の判断そのものを検証している。
 */
class AggregateBoundaryTest {

    @Test
    @DisplayName("注文は在庫を内側に持たない")
    void orderDoesNotHoldStock() {
        boolean holdsStock = Arrays.stream(Order.class.getDeclaredFields())
                .map(Field::getType)
                .map(Class::getSimpleName)
                .anyMatch(name -> name.contains("Stock") || name.contains("Inventory"));

        assertThat(holdsStock).isFalse();
    }

    @Test
    @DisplayName("明細は商品そのものではなく商品の識別子を持つ")
    void lineRefersToProductById() {
        OrderLine line = new OrderLine(new ProductId("P-100"), new Quantity(1), Money.yen(980));

        assertThat(line.productId()).isInstanceOf(ProductId.class);
        assertThat(Arrays.stream(OrderLine.class.getRecordComponents())
                        .map(c -> c.getType().getSimpleName()))
                .containsExactly("ProductId", "Quantity", "Money");
    }

    @Test
    @DisplayName("注文が外に見せる状態は、引当と決済の識別子だけを持つ")
    void orderKeepsOtherAggregatesByIdOnly() {
        Order order = Order.place(
                new OrderId("O-100"),
                new CustomerId("C-001"),
                Instant.parse("2026-08-06T10:00:00Z"));

        assertThat(order.state()).isInstanceOf(OrderState.Accepted.class);
        assertThat(Arrays.stream(OrderState.Paid.class.getRecordComponents())
                        .map(c -> c.getType().getSimpleName()))
                .contains("ReservationId", "PaymentId");
    }
}
