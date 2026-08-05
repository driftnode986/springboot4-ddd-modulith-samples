package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OptimisticLockTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderJpaRepository jpa;

    @Test
    void 同じ注文を2人が同時に更新すると後から書いた方が失敗する() {
        OrderId id = new OrderId("ORD-400");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        order.addLine(new ProductId("P-1"), new Quantity(1), Money.yen(1000));
        repository.save(order);

        // 2 人が同じ版を読む
        OrderEntity first = jpa.findWithLinesById(id.value()).orElseThrow();
        OrderEntity second = jpa.findWithLinesById(id.value()).orElseThrow();
        assertThat(first.getVersion()).isEqualTo(second.getVersion());

        // 先に書いた方は通る
        first.setState("RESERVED");
        jpa.saveAndFlush(first);

        // 後から書いた方は、読んだときの版が古いので弾かれる
        second.setState("CANCELLED");
        assertThatThrownBy(() -> jpa.saveAndFlush(second))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
