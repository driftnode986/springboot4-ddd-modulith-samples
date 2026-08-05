package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.OrderState;
import com.example.shop.order.domain.PaymentId;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import com.example.shop.order.domain.ReservationId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class JpaOrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private OrderRepository repository;

    @Test
    @DisplayName("保存した注文を識別子で取り出せる")
    void savedOrderIsFoundById() {
        OrderId id = new OrderId("ORD-100");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        order.addLine(new ProductId("P-1"), new Quantity(2), Money.yen(1500));

        repository.save(order);

        Order found = repository.findById(id).orElseThrow();
        assertThat(found.id()).isEqualTo(id);
        assertThat(found.lines()).hasSize(1);
    }

    @Test
    @DisplayName("金額を保存して読み直すと同じ金額と通貨になる")
    void moneySurvivesRoundTrip() {
        OrderId id = new OrderId("ORD-101");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        order.addLine(new ProductId("P-1"), new Quantity(1), Money.yen(1500));
        repository.save(order);

        Order found = repository.findById(id).orElseThrow();

        assertThat(found.total()).isEqualTo(Money.yen(1500));
    }

    @Test
    @DisplayName("支払済の注文を保存して読み直すと支払済のまま")
    void paidStateSurvivesRoundTrip() {
        OrderId id = new OrderId("ORD-102");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        OrderState.Accepted accepted = (OrderState.Accepted) order.state();
        OrderState.Paid paid = accepted.reserve(new ReservationId("RSV-1")).pay(new PaymentId("PAY-1"));
        Order stored = Order.restore(id, new CustomerId("CUS-1"), paid, List.of());
        repository.save(stored);

        Order found = repository.findById(id).orElseThrow();

        assertThat(found.state()).isInstanceOf(OrderState.Paid.class);
        OrderState.Paid restored = (OrderState.Paid) found.state();
        assertThat(restored.paymentId()).isEqualTo(new PaymentId("PAY-1"));
    }
}
