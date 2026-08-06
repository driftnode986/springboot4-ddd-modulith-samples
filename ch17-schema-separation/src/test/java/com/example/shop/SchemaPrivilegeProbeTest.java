package com.example.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * スキーマを分けただけでは結合を止められないこと、
 * 接続の権限を分けると止まることを確かめる。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchemaPrivilegeProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private JdbcTemplate jdbc;

    private JdbcTemplate asOrderRole() {
        jdbc.execute(
                "DO $$ BEGIN IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'order_app')"
                        + " THEN EXECUTE 'DROP OWNED BY order_app CASCADE';"
                        + " EXECUTE 'DROP ROLE order_app'; END IF; END $$");
        jdbc.execute("CREATE ROLE order_app LOGIN PASSWORD 'secret'");
        jdbc.execute("GRANT USAGE ON SCHEMA ordering TO order_app");
        jdbc.execute("GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA ordering TO order_app");

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(postgres.getJdbcUrl());
        ds.setUsername("order_app");
        ds.setPassword("secret");
        return new JdbcTemplate(ds);
    }

    @Test
    @DisplayName("注文の権限しか持たない接続では、在庫の表を読めない")
    void cannotReadOtherSchema() {
        JdbcTemplate asOrder = asOrderRole();

        assertThat(asOrder.queryForObject("SELECT count(*) FROM ordering.orders", Integer.class))
                .isNotNull();

        try {
            asOrder.queryForObject("SELECT count(*) FROM inventory.stocks", Integer.class);
        } catch (Exception denied) {
            System.out.println("DENIED_ROOT=" + rootCause(denied).getMessage());
        }

        assertThatThrownBy(
                        () ->
                                asOrder.queryForObject(
                                        "SELECT count(*) FROM inventory.stocks", Integer.class))
                .hasMessageContaining("inventory");
    }

    private static Throwable rootCause(Throwable t) {
        return t.getCause() == null ? t : rootCause(t.getCause());
    }
}
