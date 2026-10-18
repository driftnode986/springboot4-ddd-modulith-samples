package com.example.shop.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.HandledEvents;
import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
import com.example.shop.payment.spi.PaymentFailed;
import java.util.List;
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

/** 支払いが成立しなかったとき、引き当てた在庫が戻ることを確かめる。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ReleaseStockOnPaymentFailedTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private StockRepository stocks;
    @Autowired private HandledEvents handledEvents;
    @Autowired private JdbcTemplate jdbc;

    private ReserveStockOnOrderPlaced reserve;
    private ReleaseStockOnPaymentFailed release;

    private String sku;
    private String orderId;

    @BeforeEach
    void setUp() {
        sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update(
                "INSERT INTO inventory.stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
        reserve = new ReserveStockOnOrderPlaced(stocks, handledEvents);
        release = new ReleaseStockOnPaymentFailed(stocks, handledEvents);
    }

    /** 支払いが失敗する手前まで進める。引き当てが終わった状態を作る。 */
    private void reserveThree() {
        reserve.on(
                new OrderPlaced(
                        UUID.randomUUID().toString(),
                        orderId,
                        "CUS-1",
                        List.of(new OrderPlaced.Item(sku, 3))));
    }

    private PaymentFailed paymentFailed(String eventId) {
        return new PaymentFailed(
                eventId, orderId, List.of(new PaymentFailed.Item(sku, 3)), "DECLINED");
    }

    @Test
    @DisplayName("支払いが成立しなかったら、引き当てた在庫が戻る")
    void releasesReservedStock() {
        reserveThree();
        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(7);

        release.on(paymentFailed(UUID.randomUUID().toString()));

        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(10);
    }

    @Test
    @DisplayName("同じ戻しを 2 回受け取っても、在庫は 1 回しか戻らない")
    void releasesOnlyOnceForTheSameEvent() {
        reserveThree();
        PaymentFailed delivered = paymentFailed(UUID.randomUUID().toString());

        release.on(delivered);
        release.on(delivered);

        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(10);
    }
}
