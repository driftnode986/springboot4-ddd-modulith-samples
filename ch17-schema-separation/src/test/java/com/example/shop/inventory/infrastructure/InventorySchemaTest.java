package com.example.shop.inventory.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.HandledEvents;
import com.example.shop.inventory.domain.Stock;
import com.example.shop.inventory.domain.StockRepository;
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

/** 在庫モジュールの表が inventory スキーマに読み書きされることを確かめる。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class InventorySchemaTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private StockRepository stocks;
    @Autowired private HandledEvents handledEvents;
    @Autowired private JdbcTemplate jdbc;

    @Test
    @DisplayName("StockEntity は inventory.stocks に保存される")
    void savesStockIntoInventorySchema() {
        String sku = "P-" + UUID.randomUUID().toString().substring(0, 8);

        stocks.save(new Stock(sku, 5));

        assertThat(jdbc.queryForObject(
                        "SELECT quantity FROM inventory.stocks WHERE sku = ?", Integer.class, sku))
                .isEqualTo(5);
        assertThat(stocks.findBySku(sku)).get().extracting("quantity").isEqualTo(5);
    }

    @Test
    @DisplayName("recordIfFirst は inventory.handled_events に記録し、二度目は false を返す")
    void recordsHandledEventsIntoInventorySchema() {
        String eventId = UUID.randomUUID().toString();

        assertThat(handledEvents.recordIfFirst(eventId, "test")).isTrue();
        assertThat(handledEvents.recordIfFirst(eventId, "test")).isFalse();

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM inventory.handled_events WHERE event_id = ?",
                        Integer.class,
                        eventId))
                .isEqualTo(1);
    }
}
