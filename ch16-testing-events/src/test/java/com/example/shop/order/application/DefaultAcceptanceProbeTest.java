package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 待つ条件を省いたときに何が起きるかを確かめる。
 *
 * <p>もとから値を持っている対象では、変化を待たずに通ってしまう。
 */
@SpringBootTest
@EnableScenarios
@Testcontainers(disabledWithoutDocker = true)
class DefaultAcceptanceProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;
    @Autowired private StockRepository stocks;
    @Autowired private JdbcTemplate jdbc;

    private String sku;

    @BeforeEach
    void setUp() {
        sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
    }

    @Test
    @DisplayName("条件を省くと、変化する前の値のまま通ってしまう")
    void passesWithoutWaitingWhenAcceptanceOmitted(Scenario scenario) {
        PlaceOrderCommand command =
                new PlaceOrderCommand(
                        "CUS-1", "東京都千代田区1-1", List.of(new PlaceOrderCommand.Line(sku, 3, 1200L)));

        scenario.stimulate(() -> placeOrder.place(command))
                .andWaitForStateChange(() -> stocks.findBySku(sku).orElseThrow().quantity())
                .andVerify(
                        quantity -> {
                            System.out.println("PROBE_DEFAULT_ACCEPTANCE_SAW=" + quantity);
                            assertThat(quantity).isBetween(7, 10);
                        });
    }
}
