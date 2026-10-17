package com.example.shop.steps.beforeschema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 第17章の冒頭の形。スキーマを分ける前の表（第16章までの移行スクリプトの写し。
 * src/test/resources/steps/beforeschema/db）に対して、注文と在庫を結合する。
 *
 * <p>完成形（スキーマを分けたあと）は com.example.shop.CrossModuleSqlProbeTest。
 */
@Testcontainers(disabledWithoutDocker = true)
class CrossModuleSqlProbeTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    private JdbcTemplate jdbc;
    private String orderId;

    @BeforeEach
    void setUp() {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(),
                        postgres.getUsername(), postgres.getPassword())
                .locations("classpath:steps/beforeschema/db")
                .load()
                .migrate();
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(),
                postgres.getUsername(), postgres.getPassword()));

        String sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        orderId = UUID.randomUUID().toString();
        jdbc.update(
                "INSERT INTO stocks (sku, quantity, version)"
                        + " VALUES (?, ?, 0)",
                sku, 10);
        jdbc.update(
                "INSERT INTO orders (id, customer_id, state, version)"
                        + " VALUES (?, ?, 'ACCEPTED', 0)",
                orderId, "CUS-1");
        jdbc.update(
                "INSERT INTO order_lines (order_id, product_id,"
                        + " quantity, unit_amount, unit_currency)"
                        + " VALUES (?, ?, 3, 1000, 'JPY')",
                orderId, sku);
    }

    // #region join
    @Test
    @DisplayName("注文の表と在庫の表は、"
            + "1 つの SQL で結合できてしまう")
    void joinsAcrossModules() {
        // 注文 1 件と、その明細が指す在庫は setUp で用意する

        Integer remaining = jdbc.queryForObject(
                "SELECT s.quantity FROM orders o"
                        + " JOIN order_lines l ON l.order_id = o.id"
                        + " JOIN stocks s ON s.sku = l.product_id"
                        + " WHERE o.id = ?",
                Integer.class,
                orderId);

        assertThat(remaining).isEqualTo(10);
    }
    // #endregion
}
