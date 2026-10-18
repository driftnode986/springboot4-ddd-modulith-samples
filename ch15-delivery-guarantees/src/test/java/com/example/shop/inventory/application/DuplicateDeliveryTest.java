package com.example.shop.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.HandledEvents;
import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
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

/** 同じイベントが 2 回届いても、在庫が 2 回減らないことを確かめる。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class DuplicateDeliveryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private StockRepository stocks;
    @Autowired private HandledEvents handledEvents;
    @Autowired private JdbcTemplate jdbc;

    /**
     * 受信側を直接組み立てて呼ぶ。コンテナから取り出したものを呼ぶと
     * {@code @ApplicationModuleListener} の {@code @Async} が働いて別スレッドに逃げ、
     * 検査が引き当ての前に走ってしまう。ここで測りたいのは重複の扱いだけなので、
     * 素のオブジェクトを同じスレッドで呼ぶ。
     */
    private ReserveStockOnOrderPlaced listener;

    private String sku;

    @BeforeEach
    void setUp() {
        sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
        listener = new ReserveStockOnOrderPlaced(stocks, handledEvents);
    }

    private OrderPlaced event(String eventId) {
        return new OrderPlaced(
                eventId, "ORD-1", "CUS-1", List.of(new OrderPlaced.Item(sku, 3)));
    }

    @Test
    @DisplayName("同じイベントを 2 回受け取っても、在庫は 1 回しか減らない")
    void reservesOnlyOnceForTheSameEvent() {
        OrderPlaced delivered = event(UUID.randomUUID().toString());

        listener.on(delivered);
        listener.on(delivered);

        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(7);
    }

    @Test
    @DisplayName("別のイベントなら、それぞれ引き当てる")
    void reservesForEachDistinctEvent() {
        listener.on(event(UUID.randomUUID().toString()));
        listener.on(event(UUID.randomUUID().toString()));

        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(4);
    }
}
