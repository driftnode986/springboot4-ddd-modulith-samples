package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import java.time.Instant;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManagerFactory;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class OrderFetchSqlCountTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private OrderRepository repository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void 明細付きの注文を取り出すと発行される問い合わせは1本() {
        OrderId id = new OrderId("ORD-200");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        order.addLine(new ProductId("P-1"), new Quantity(1), Money.yen(1000));
        order.addLine(new ProductId("P-2"), new Quantity(2), Money.yen(2000));
        order.addLine(new ProductId("P-3"), new Quantity(3), Money.yen(3000));
        repository.save(order);

        Statistics statistics =
                entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        Order found = repository.findById(id).orElseThrow();

        assertThat(found.lines()).hasSize(3);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
