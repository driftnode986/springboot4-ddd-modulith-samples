package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.inventory.application.ReserveStockOnOrderPlaced;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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

    @Autowired
    private PlaceOrderService placeOrder;

    @Autowired
    private OrderRepository orders;

    private PlaceOrderCommand command(String customerId) {
        return new PlaceOrderCommand(
                customerId,
                "東京都千代田区1-1-1",
                List.of(new PlaceOrderCommand.Line("P-1", 1, 1500)));
    }

    @Test
    @DisplayName("注文を受け付けると、在庫の引き当てがあとから動く")
    void listenerRunsAfterOrderIsPlaced() {
        OrderId id = placeOrder.place(command("CUS-EVENT-1"));

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(
                        () ->
                                assertThat(ReserveStockOnOrderPlaced.reserved)
                                        .contains(id.value()));
    }

    @Test
    @DisplayName("受信側が失敗しても、発行元の注文は残る")
    void publisherIsNotRolledBackWhenListenerFails() {
        ReserveStockOnOrderPlaced.failNext = true;

        OrderId id = placeOrder.place(command("CUS-EVENT-2"));

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(
                        () -> assertThat(ReserveStockOnOrderPlaced.failed).contains(id.value()));

        // 受信側は落ちた。それでも注文はコミット済みなので残っている。
        assertThat(orders.findById(id)).isPresent();
    }
}
