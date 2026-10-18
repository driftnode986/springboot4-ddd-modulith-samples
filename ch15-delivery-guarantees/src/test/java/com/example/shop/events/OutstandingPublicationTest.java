package com.example.shop.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.application.PlaceOrderService;
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

/** 受け取る側が完了しなかったイベントが、レジストリの表に残ることを確かめる。 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OutstandingPublicationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM event_publication");
    }

    private PlaceOrderCommand command(String customerId, String productId) {
        return new PlaceOrderCommand(
                customerId,
                "東京都千代田区1-1-1",
                List.of(new PlaceOrderCommand.Line(productId, 1, 1500)));
    }

    @Test
    @DisplayName("受け取る側が成功すると、完了の時刻が記録される")
    void publicationIsCompletedWhenListenerSucceeds() {
        String sku = "P-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO stocks (sku, quantity, version) VALUES (?, ?, 0)", sku, 10);

        placeOrder.place(command("CUS-REG-1", sku));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () ->
                                assertThat(
                                                jdbc.queryForObject(
                                                        "SELECT count(*) FROM event_publication"
                                                                + " WHERE completion_date IS NOT NULL",
                                                        Integer.class))
                                        .isEqualTo(1));
    }

    @Test
    @DisplayName("受け取る側が失敗すると、イベントは未完了のまま表に残る")
    void publicationStaysOutstandingWhenListenerFails() {
        // 在庫のない商品を注文する。受け取る側は引き当てに失敗する。
        String missing = "P-" + UUID.randomUUID().toString().substring(0, 8);

        placeOrder.place(command("CUS-REG-2", missing));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(
                        () ->
                                assertThat(
                                                jdbc.queryForObject(
                                                        "SELECT count(*) FROM event_publication"
                                                                + " WHERE completion_date IS NULL",
                                                        Integer.class))
                                        .isEqualTo(1));

        jdbc.queryForList(
                        "SELECT listener_id, event_type, status, serialized_event FROM event_publication")
                .forEach(row -> System.out.println("OUTSTANDING=" + row));
    }
}
