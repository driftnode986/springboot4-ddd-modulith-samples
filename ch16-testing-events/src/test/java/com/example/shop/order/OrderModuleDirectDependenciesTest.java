package com.example.shop.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.application.PlaceOrderService;
import com.example.shop.order.spi.OrderPlaced;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.ApplicationModuleTest.BootstrapMode;
import org.springframework.modulith.test.Scenario;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 起動する範囲を直接の依存まで広げると、出荷を差し替えなくても注文モジュールが起動することを確かめる。
 */
// #region direct-dependencies
@ApplicationModuleTest(
        mode = BootstrapMode.DIRECT_DEPENDENCIES,
        verifyAutomatically = false)
// #endregion
@Testcontainers(disabledWithoutDocker = true)
class OrderModuleDirectDependenciesTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;

    @Test
    @DisplayName("直接の依存まで起動すると、差し替えなしで注文成立の事実が発行される")
    void publishesOrderPlacedWithoutMocks(Scenario scenario) {
        PlaceOrderCommand command =
                new PlaceOrderCommand(
                        "CUS-2",
                        "東京都千代田区1-1",
                        List.of(new PlaceOrderCommand.Line(
                                "P-0002", 1, 1200L)));

        scenario.stimulate(() -> placeOrder.place(command))
                .andWaitForEventOfType(OrderPlaced.class)
                .matchingMappedValue(
                        OrderPlaced::customerId, "CUS-2")
                .toArriveAndVerify(event ->
                        assertThat(event.items()).hasSize(1));
    }
}
