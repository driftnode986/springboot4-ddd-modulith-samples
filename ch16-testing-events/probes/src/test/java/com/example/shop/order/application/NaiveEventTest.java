package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.PublishedEvents;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 確認用。src/test の同名のファイルをこのファイルで置き換えて ./gradlew test を
 * 走らせると、seesThePublication がテストの本体より前に
 * ParameterResolutionException で落ちる（@SpringBootTest だけでは
 * PublishedEvents を引数で受け取れない）。確かめたら元に戻す。
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class NaiveEventTest {

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

    private PlaceOrderCommand command() {
        return new PlaceOrderCommand(
                "CUS-1", "東京都千代田区1-1", List.of(new PlaceOrderCommand.Line(sku, 3, 1200L)));
    }

    // #region published-events-arg
    // 引数で受け取る形。ただしこのままでは動かない（理由は直後）
    @Test
    void seesThePublication(PublishedEvents events) {
        placeOrder.place(command());

        assertThat(events.ofType(OrderPlaced.class)).hasSize(1);
    }
    // #endregion
}
