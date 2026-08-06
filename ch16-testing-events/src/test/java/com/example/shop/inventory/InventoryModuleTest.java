package com.example.shop.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 在庫モジュールだけを起動して、受け取る側の動きを確かめる。
 *
 * <p>注文モジュールは起動しない。イベントは直接投げ込む。
 */
@ApplicationModuleTest(verifyAutomatically = false)
@Testcontainers(disabledWithoutDocker = true)
class InventoryModuleTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private StockRepository stocks;
    @Autowired private JdbcTemplate jdbc;

    private String sku;

    @BeforeEach
    void setUp() {
        sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
    }

    @Test
    @DisplayName("イベントを投げ込むと、在庫が引き当てられる")
    void reservesOnIncomingEvent(Scenario scenario) {
        OrderPlaced event =
                new OrderPlaced(
                        UUID.randomUUID().toString(),
                        "ORD-1",
                        "CUS-1",
                        List.of(new OrderPlaced.Item(sku, 3)));

        scenario.publish(event)
                .andWaitForStateChange(() -> stocks.findBySku(sku).orElseThrow().quantity(), q -> q < 10)
                .andVerify(quantity -> assertThat(quantity).isEqualTo(7));
    }
}
