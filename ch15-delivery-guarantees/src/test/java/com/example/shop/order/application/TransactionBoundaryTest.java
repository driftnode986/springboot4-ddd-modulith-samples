package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
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

/** イベントで繋いだとき、失敗が巻き戻す範囲がどこまでかを確かめる。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class TransactionBoundaryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;
    @Autowired private OrderRepository orders;
    @Autowired private StockRepository stocks;
    @Autowired private JdbcTemplate jdbc;

    private String sku;

    @BeforeEach
    void setUp() {
        sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);
    }

    private PlaceOrderCommand command(String customerId, String productId) {
        return new PlaceOrderCommand(
                customerId,
                "東京都千代田区1-1-1",
                List.of(new PlaceOrderCommand.Line(productId, 1, 1500)));
    }

    @Test
    @DisplayName("注文を受け付けると、在庫の引き当てがあとから動く")
    void listenerRunsAfterOrderIsPlaced() {
        placeOrder.place(command("CUS-EVENT-1", sku));

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(
                        () -> assertThat(stocks.findBySku(sku)).get()
                                .extracting("quantity")
                                .isEqualTo(9));
    }

    @Test
    @DisplayName("受信側が失敗しても、発行元の注文は残る")
    void publisherIsNotRolledBackWhenListenerFails() {
        // 在庫のない商品を注文する。受信側は引き当てに失敗する。
        String missing = "P-" + UUID.randomUUID().toString().substring(0, 8);

        OrderId id = placeOrder.place(command("CUS-EVENT-2", missing));

        // 受信側が動く時間を与える。
        Awaitility.await()
                .during(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(orders.findById(id)).isPresent());

        // 受信側は例外で終わった。それでも注文はコミット済みなので残っている。
        assertThat(orders.findById(id)).isPresent();
        assertThat(stocks.findBySku(missing)).isEmpty();
    }
}
