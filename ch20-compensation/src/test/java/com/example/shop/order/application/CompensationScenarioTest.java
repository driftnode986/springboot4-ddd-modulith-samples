package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.domain.OrderId;
import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.PaymentGateway;
import com.example.shop.payment.domain.StubPaymentGateway;
import com.example.shop.payment.spi.PaymentFailed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** 支払いが成立しなかったときに、引き当てた在庫が戻るまでを通しで確かめる。 */
@SpringBootTest
@EnableScenarios
@Testcontainers(disabledWithoutDocker = true)
class CompensationScenarioTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    /** 外部の決済サービスには出ない。必ず断られる相手として振る舞わせる。 */
    @TestConfiguration
    static class DecliningGateway {
        @Bean
        @Primary
        PaymentGateway declining() {
            return new StubPaymentGateway(ChargeResult.DECLINED);
        }
    }

    @Autowired private PlaceOrderService placeOrder;
    @Autowired private StockRepository stocks;
    @Autowired private JdbcTemplate jdbc;

    private String sku;

    @BeforeEach
    void setUp() {
        sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update(
                "INSERT INTO inventory.stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
    }

    private PlaceOrderCommand command() {
        return new PlaceOrderCommand(
                "CUS-1", "東京都千代田区1-1", List.of(new PlaceOrderCommand.Line(sku, 3, 1200L)));
    }

    @Test
    @DisplayName("支払いが断られると、引き当てた在庫が戻るまで待って確かめられる")
    void releasesStockEventually(Scenario scenario) {
        OrderId orderId = placeOrder.place(command());

        // 引き当てが終わるまで待つ。この時点で在庫は別のトランザクションで確定している。
        scenario.stimulate(() -> {})
                .andWaitForStateChange(
                        () -> stocks.findBySku(sku).orElseThrow().quantity(), q -> q == 7)
                .andVerify(quantity -> assertThat(quantity).isEqualTo(7));

        scenario.stimulate(() -> placeOrder.requestPayment(orderId))
                .andWaitForEventOfType(PaymentFailed.class)
                .toArriveAndVerify(
                        event -> assertThat(event.items()).extracting("productId").containsExactly(sku));

        scenario.stimulate(() -> {})
                .andWaitForStateChange(
                        () -> stocks.findBySku(sku).orElseThrow().quantity(), q -> q == 10)
                .andVerify(quantity -> assertThat(quantity).isEqualTo(10));
    }
}
