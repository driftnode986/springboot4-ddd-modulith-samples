package com.example.shop.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.application.PlaceOrderService;
import com.example.shop.order.spi.OrderPlaced;
import com.example.shop.payment.spi.PaymentRequest;
import com.example.shop.shipping.spi.ShipmentArrangement;
import com.example.shop.shipping.spi.ShipmentId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 注文モジュールだけを起動して、発行される事実を確かめる。
 *
 * <p>出荷と支払いは別モジュールなので起動しない。差し替えて満たす。
 */
@ApplicationModuleTest(verifyAutomatically = false)
@Testcontainers(disabledWithoutDocker = true)
class OrderModuleTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired private PlaceOrderService placeOrder;

    @MockitoBean private ShipmentArrangement shipments;
    @MockitoBean private PaymentRequest payments;

    @Test
    @DisplayName("注文が成立すると、注文成立の事実が発行される")
    void publishesOrderPlaced(Scenario scenario) {
        BDDMockito.given(shipments.arrange(BDDMockito.anyString(), BDDMockito.anyString()))
                .willReturn(new ShipmentId(UUID.randomUUID().toString()));

        PlaceOrderCommand command =
                new PlaceOrderCommand(
                        "CUS-1",
                        "東京都千代田区1-1",
                        List.of(new PlaceOrderCommand.Line("P-0001", 3, 1200L)));

        scenario.stimulate(() -> placeOrder.place(command))
                .andWaitForEventOfType(OrderPlaced.class)
                .matchingMappedValue(OrderPlaced::customerId, "CUS-1")
                .toArriveAndVerify(event -> assertThat(event.items()).hasSize(1));
    }
}
