package com.example.steps.ch12.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.shipping.spi.ShipmentId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 第12章までの版: 注文を保存して、出荷を直接呼ぶ。 */
class PlaceOrderServiceTest {

    private final Map<OrderId, Order> store = new HashMap<>();
    private final List<String> arranged = new ArrayList<>();

    private final OrderRepository orders = new OrderRepository() {
        @Override
        public void save(Order order) {
            store.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(store.get(id));
        }
    };

    private final PlaceOrderService service = new PlaceOrderService(
            orders,
            Clock.fixed(Instant.parse("2026-08-06T00:00:00Z"), ZoneOffset.UTC),
            (orderId, address) -> {
                arranged.add(orderId);
                return new ShipmentId("S-" + orderId);
            });

    @Test
    @DisplayName("注文を保存し、同じ注文の出荷を直接手配する")
    void savesOrderAndArrangesShipment() {
        OrderId id = service.place(new PlaceOrderCommand(
                "C-001", "東京都千代田区1-1",
                List.of(new PlaceOrderCommand.Line("P-001", 2, 1500))));

        assertThat(store.get(id).total()).isEqualTo(Money.yen(3000));
        assertThat(arranged).containsExactly(id.value());
    }

    @Test
    @DisplayName("明細が空の注文は受け付けない")
    void rejectsEmptyOrder() {
        assertThatThrownBy(() -> service.place(
                new PlaceOrderCommand("C-001", "東京都千代田区1-1", List.of())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(arranged).isEmpty();
    }
}
