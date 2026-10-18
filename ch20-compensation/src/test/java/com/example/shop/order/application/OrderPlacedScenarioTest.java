package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
import java.time.Duration;
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

/** 注文から在庫の引き当てまでを、結果が出るまで待って確かめる。 */
@SpringBootTest
@EnableScenarios
@Testcontainers(disabledWithoutDocker = true)
class OrderPlacedScenarioTest {

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
        jdbc.update("INSERT INTO inventory.stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
    }

    private PlaceOrderCommand command() {
        return new PlaceOrderCommand(
                "CUS-1", "東京都千代田区1-1", List.of(new PlaceOrderCommand.Line(sku, 3, 1200L)));
    }

    @Test
    @DisplayName("注文すると、在庫が引き当てられるまで待って確かめられる")
    void reservesStockEventually(Scenario scenario) {
        scenario.stimulate(() -> placeOrder.place(command()))
                .andWaitForStateChange(() -> stocks.findBySku(sku).orElseThrow().quantity(), q -> q < 10)
                .andVerify(quantity -> assertThat(quantity).isEqualTo(7));
    }

    @Test
    @DisplayName("発行された事実そのものを確かめる")
    void publishesOrderPlaced(Scenario scenario) {
        scenario.stimulate(() -> placeOrder.place(command()))
                .andWaitForEventOfType(OrderPlaced.class)
                .matchingMappedValue(OrderPlaced::customerId, "CUS-1")
                .toArriveAndVerify(
                        event -> assertThat(event.items()).extracting("productId").containsExactly(sku));
    }

    @Test
    @DisplayName("待つ時間の上限を指定できる")
    void canBoundTheWait(Scenario scenario) {
        scenario.stimulate(() -> placeOrder.place(command()))
                .andWaitAtMost(Duration.ofSeconds(5))
                .andWaitForStateChange(() -> stocks.findBySku(sku).orElseThrow().quantity(), q -> q < 10)
                .andVerify(quantity -> assertThat(quantity).isEqualTo(7));
    }
}
