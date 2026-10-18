package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final CustomerId CUSTOMER = new CustomerId("C-001");
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("明細を足すと合計が変わる")
    void totalChangesWhenLineAdded() {
        Order order = Order.place(new OrderId("O-001"), CUSTOMER, NOW);

        order.addLine(new ProductId("P-100"), new Quantity(2), Money.yen(980));

        assertThat(order.total()).isEqualTo(Money.yen(1960));
    }

    @Test
    @DisplayName("明細を足すたびに合計が積み上がる")
    void totalAccumulates() {
        Order order = Order.place(new OrderId("O-002"), CUSTOMER, NOW);

        order.addLine(new ProductId("P-100"), new Quantity(2), Money.yen(980));
        order.addLine(new ProductId("P-200"), new Quantity(1), Money.yen(1500));

        assertThat(order.total()).isEqualTo(Money.yen(3460));
    }

    @Test
    @DisplayName("明細が1件もない注文の合計は0円")
    void emptyOrderHasZeroTotal() {
        Order order = Order.place(new OrderId("O-003"), CUSTOMER, NOW);

        assertThat(order.total()).isEqualTo(Money.yen(0));
    }

    @Test
    @DisplayName("取り出した明細を変更しても注文の中身は変わらない")
    void linesAreNotWritableFromOutside() {
        Order order = Order.place(new OrderId("O-004"), CUSTOMER, NOW);
        order.addLine(new ProductId("P-100"), new Quantity(1), Money.yen(980));

        assertThatThrownBy(() -> order.lines().add(
                        new OrderLine(new ProductId("P-999"), new Quantity(1), Money.yen(1))))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(order.lines()).hasSize(1);
        assertThat(order.total()).isEqualTo(Money.yen(980));
    }

    @Test
    @DisplayName("合計が上限を超える明細は追加できない")
    void rejectsLineThatExceedsLimit() {
        Order order = Order.place(new OrderId("O-005"), CUSTOMER, NOW);
        order.addLine(new ProductId("P-100"), new Quantity(1), Money.yen(400_000));

        assertThatThrownBy(() ->
                        order.addLine(new ProductId("P-200"), new Quantity(1), Money.yen(200_000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("上限");
    }

    @Test
    @DisplayName("上限を超える追加を拒んでも、それまでの明細は残る")
    void keepsExistingLinesAfterRejection() {
        Order order = Order.place(new OrderId("O-006"), CUSTOMER, NOW);
        order.addLine(new ProductId("P-100"), new Quantity(1), Money.yen(400_000));

        assertThatThrownBy(() ->
                        order.addLine(new ProductId("P-200"), new Quantity(1), Money.yen(200_000)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(order.lines()).hasSize(1);
        assertThat(order.total()).isEqualTo(Money.yen(400_000));
    }

    @Test
    @DisplayName("同じ識別子の注文は、明細が違っても同じ注文として扱う")
    void identityIsDecidedByIdAlone() {
        Order one = Order.place(new OrderId("O-007"), CUSTOMER, NOW);
        Order other = Order.place(new OrderId("O-007"), CUSTOMER, NOW);
        other.addLine(new ProductId("P-100"), new Quantity(1), Money.yen(980));

        assertThat(one).isEqualTo(other);
        assertThat(one).hasSameHashCodeAs(other);
    }

    @Test
    @DisplayName("識別子が違えば別の注文として扱う")
    void differentIdsAreDifferentOrders() {
        Order one = Order.place(new OrderId("O-008"), CUSTOMER, NOW);
        Order other = Order.place(new OrderId("O-009"), CUSTOMER, NOW);

        assertThat(one).isNotEqualTo(other);
    }

    @Test
    @DisplayName("受け付けた直後の注文は Accepted 状態にある")
    void placedOrderIsAccepted() {
        Order order = Order.place(new OrderId("O-010"), CUSTOMER, NOW);

        assertThat(order.state()).isInstanceOf(OrderState.Accepted.class);
    }
}
