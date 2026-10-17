package com.example.shop.order.domain.steps.nolimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderState;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 上限の規則を入れる前の注文（本文の途中の版）の振る舞い。 */
class OrderTest {

    private static final CustomerId CUSTOMER = new CustomerId("C-001");
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");

    @Test
    @DisplayName("途中の版: 明細を足すと合計が変わる")
    void totalChangesWhenLineAdded() {
        Order order = Order.place(new OrderId("O-001"), CUSTOMER, NOW);

        order.addLine(new ProductId("P-100"), new Quantity(2), Money.yen(980));

        assertThat(order.total()).isEqualTo(Money.yen(1960));
        assertThat(order.id()).isEqualTo(new OrderId("O-001"));
        assertThat(order.customerId()).isEqualTo(CUSTOMER);
        assertThat(order.state()).isInstanceOf(OrderState.Accepted.class);
    }

    @Test
    @DisplayName("途中の版: 同じ識別子なら明細が違っても同じ注文")
    void identityIsDecidedByIdAlone() {
        Order one = Order.place(new OrderId("O-007"), CUSTOMER, NOW);
        Order other = Order.place(new OrderId("O-007"), CUSTOMER, NOW);
        other.addLine(new ProductId("P-100"), new Quantity(1), Money.yen(980));

        assertThat(one).isEqualTo(other);
        assertThat(one).hasSameHashCodeAs(other);
    }

    @Test
    @DisplayName("途中の版: まだ上限を見ないので、上限を超える明細も入る")
    void doesNotCheckLimitYet() {
        Order order = Order.place(new OrderId("O-005"), CUSTOMER, NOW);
        order.addLine(new ProductId("P-100"), new Quantity(1), Money.yen(400_000));

        order.addLine(new ProductId("P-200"), new Quantity(1), Money.yen(200_000));

        assertThat(order.total()).isEqualTo(Money.yen(600_000));
    }
}
