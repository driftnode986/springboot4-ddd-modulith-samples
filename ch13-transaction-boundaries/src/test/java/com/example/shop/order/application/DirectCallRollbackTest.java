package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.shipping.spi.ShipmentArrangement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 直接呼び出しで繋いだとき、呼ばれた側の失敗がどこまで巻き戻すかを確かめる。
 *
 * <p>イベントで繋いだ場合との違いは {@code TransactionBoundaryTest} と読み比べる。
 */
// #region setup
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class DirectCallRollbackTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;
    @Autowired private OrderRepository orders;

    @MockitoBean private ShipmentArrangement shipments;
    // #endregion

    // #region test
    @Test
    @DisplayName("直接呼び出した先が失敗すると、"
            + "発行元の注文も残らない")
    void publisherIsRolledBackWhenCalleeFails() {
        List<String> attempted = new ArrayList<>();
        when(shipments.arrange(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    attempted.add(invocation.getArgument(0));
                    throw new IllegalStateException(
                            "出荷の手配に失敗しました");
                });

        PlaceOrderCommand command = new PlaceOrderCommand(
                "CUS-DIRECT-1",
                "東京都千代田区1-1-1",
                List.of(new PlaceOrderCommand.Line(
                        "P-1", 1, 1500)));

        assertThatThrownBy(() -> placeOrder.place(command))
                .isInstanceOf(IllegalStateException.class);

        assertThat(attempted).hasSize(1);
        assertThat(orders.findById(
                new OrderId(attempted.get(0)))).isEmpty();
    }
}
// #endregion
