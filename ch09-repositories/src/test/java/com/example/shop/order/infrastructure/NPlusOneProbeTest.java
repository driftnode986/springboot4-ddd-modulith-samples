package com.example.shop.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** fetch join を使わないと問い合わせが何本になるかの確認用。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class NPlusOneProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderJpaRepository jpa;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("fetch_join_を使わないと注文と明細で問い合わせが2本になる")
    void withoutFetchJoinTwoStatementsAreIssued() {
        OrderId id = new OrderId("ORD-300");
        Order order = Order.place(id, new CustomerId("CUS-1"), Instant.parse("2026-01-01T00:00:00Z"));
        order.addLine(new ProductId("P-1"), new Quantity(1), Money.yen(1000));
        order.addLine(new ProductId("P-2"), new Quantity(2), Money.yen(2000));
        repository.save(order);

        // 保存したものが持ち越されていると問い合わせが出ないので、
        // いったん書き出してから持ち越しを捨てる。
        entityManager.flush();
        entityManager.clear();

        Statistics statistics =
                entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        OrderEntity entity = jpa.findById(id.value()).orElseThrow();
        long afterFind = statistics.getPrepareStatementCount();
        int size = entity.getLines().size();
        long afterLines = statistics.getPrepareStatementCount();

        assertThat(size).isEqualTo(2);
        System.out.println("注文だけ = " + afterFind + " 本 / 明細に触れたあと = " + afterLines + " 本");
    }
}
