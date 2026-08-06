package com.example.shop.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.HandledEvents;
import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
import java.util.List;
import java.util.UUID;
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
 * 記録を持たない受信側が、重複でどうなるかを測るための確認用テスト。
 *
 * <p>本文の「重複すると 2 回減る」という主張の裏を取る。読者が書くものではない。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class UnguardedDuplicateProbeTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private StockRepository stocks;
    @Autowired private JdbcTemplate jdbc;

    /** 何度でも「はじめて」と答える記録。冪等性を外した状態を作る。 */
    private static final HandledEvents ALWAYS_FIRST = (eventId, handler) -> true;

    @Test
    @DisplayName("記録を持たないと、同じイベントで在庫が 2 回減る")
    void reservesTwiceWithoutTheRecord() {
        String sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO inventory.stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);

        ReserveStockOnOrderPlaced unguarded =
                new ReserveStockOnOrderPlaced(stocks, ALWAYS_FIRST);
        OrderPlaced delivered =
                new OrderPlaced(
                        UUID.randomUUID().toString(),
                        "ORD-1",
                        "CUS-1",
                        List.of(new OrderPlaced.Item(sku, 3)));

        unguarded.on(delivered);
        unguarded.on(delivered);

        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(4);
    }
}
