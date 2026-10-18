package com.example.shop;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * コードの境界を守っていても、データベース越しには繋がってしまうことを
 * 確かめる（スキーマを分けたあとの形）。
 *
 * <p>スキーマを分ける前の形は steps/beforeschema にある。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class CrossModuleSqlProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    @Autowired private JdbcTemplate jdbc;

    private String orderId;

    @BeforeEach
    void setUp() {
        String sku = "P-"
                + UUID.randomUUID().toString().substring(0, 8);
        orderId = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO inventory.stocks"
                        + " (sku, quantity, version)"
                        + " VALUES (?, ?, 0)",
                sku, 10);
        jdbc.update(
                "INSERT INTO ordering.orders"
                        + " (id, customer_id, state, version)"
                        + " VALUES (?, ?, 'ACCEPTED', 0)",
                orderId, "CUS-1");
        jdbc.update(
                "INSERT INTO ordering.order_lines"
                        + " (order_id, product_id, quantity,"
                        + " unit_amount, unit_currency)"
                        + " VALUES (?, ?, 3, 1000, 'JPY')",
                orderId, sku);
    }

    @Test
    @DisplayName("注文の表と在庫の表は、"
            + "1 つの SQL で結合できてしまう")
    void joinsAcrossModules() {
        Integer remaining = jdbc.queryForObject(
                // #region joined-sql
                "SELECT s.quantity FROM ordering.orders o"
                        + " JOIN ordering.order_lines l"
                        + " ON l.order_id = o.id"
                        + " JOIN inventory.stocks s"
                        + " ON s.sku = l.product_id"
                // #endregion
                        + " WHERE o.id = ?",
                Integer.class,
                orderId);

        assertThat(remaining).isEqualTo(10);
    }
}
