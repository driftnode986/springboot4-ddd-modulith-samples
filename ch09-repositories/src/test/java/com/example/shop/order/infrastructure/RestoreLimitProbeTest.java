package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 読み戻しが業務の規則を通らないことの確認用。
 * 規則を厳しくしたあとでも、保存済みの注文は読み出せなければならない。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class RestoreLimitProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderJpaRepository jpa;

    @Test
    @DisplayName("上限を超える注文が表にあっても読み戻せる")
    void orderOverLimitIsStillRestorable() {
        // 上限を引き下げたあとの既存データに相当する状態を、表に直接作る
        OrderEntity entity = new OrderEntity("ORD-OVER");
        entity.setCustomerId("CUS-1");
        entity.setState("ACCEPTED");
        entity.setAcceptedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.getLines().add(new OrderLineEntity("P-1", 1, new BigDecimal("400000.00"), "JPY"));
        entity.getLines().add(new OrderLineEntity("P-2", 1, new BigDecimal("400000.00"), "JPY"));
        jpa.saveAndFlush(entity);

        Order found = repository.findById(new OrderId("ORD-OVER")).orElseThrow();

        assertThat(found.total()).isEqualTo(Money.yen(800_000));
    }
}
