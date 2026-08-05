package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InMemoryOrderRepositoryTest {

    private final OrderRepository repository = new InMemoryOrderRepository();

    @Test
    void 保存した注文を識別子で取り出せる() {
        OrderId id = new OrderId("ORD-1");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        order.addLine(new ProductId("P-1"), new Quantity(2), Money.yen(1500));

        repository.save(order);

        Order found = repository.findById(id).orElseThrow();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.total()).isEqualTo(Money.yen(3000));
    }

    @Test
    void 存在しない識別子では空が返る() {
        assertThat(repository.findById(new OrderId("ORD-404"))).isEmpty();
    }
}
