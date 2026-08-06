package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.OrderState;
import com.example.shop.shipping.spi.ShipmentArrangement;
import com.example.shop.shipping.spi.ShipmentId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceOrderServiceTest {

    private static final Instant FIXED = Instant.parse("2026-08-06T00:00:00Z");

    private RecordingOrderRepository repository;
    private PlaceOrderService service;

    @BeforeEach
    void setUp() {
        repository = new RecordingOrderRepository();
        service =
                new PlaceOrderService(
                        repository,
                        Clock.fixed(FIXED, ZoneOffset.UTC),
                        new RecordingShipmentArrangement());
    }

    @Test
    @DisplayName("注文を受け付けると、保存された注文を識別子で取り出せる")
    void placesOrder() {
        PlaceOrderCommand command = new PlaceOrderCommand(
                "C-001", "東京都千代田区1-1", List.of(new PlaceOrderCommand.Line("P-001", 2, 1500)));

        OrderId id = service.place(command);

        Optional<Order> saved = repository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().customerId()).isEqualTo(new CustomerId("C-001"));
        assertThat(saved.get().total()).isEqualTo(Money.yen(3000));
        assertThat(saved.get().state()).isInstanceOf(OrderState.Accepted.class);
    }

    @Test
    @DisplayName("明細が空の注文は受け付けない")
    void rejectsEmptyOrder() {
        PlaceOrderCommand command = new PlaceOrderCommand("C-001", "東京都千代田区1-1", List.of());

        assertThatThrownBy(() -> service.place(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("明細が 1 件もありません");
    }

    @Test
    @DisplayName("上限を超える注文は受け付けず、何も保存しない")
    void rejectsOverLimitAndSavesNothing() {
        PlaceOrderCommand command = new PlaceOrderCommand(
                "C-001",
                "東京都千代田区1-1",
                List.of(new PlaceOrderCommand.Line("P-001", 1, 800_000)));

        assertThatThrownBy(() -> service.place(command))
                .isInstanceOf(IllegalStateException.class);

        assertThat(repository.saveCount()).isZero();
    }

    /** 保存が呼ばれた回数まで見たいので、第9章のインメモリ実装に数えるだけの機能を足した。 */
    static class RecordingOrderRepository implements OrderRepository {

        private final Map<OrderId, Order> store = new HashMap<>();
        private int saveCount;

        @Override
        public void save(Order order) {
            saveCount++;
            store.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            return Optional.ofNullable(store.get(id));
        }

        int saveCount() {
            return saveCount;
        }
    }

    /** 出荷の手配を記録するだけのスタブ。 */
    static final class RecordingShipmentArrangement implements ShipmentArrangement {

        @Override
        public ShipmentId arrange(String orderId, String address) {
            return new ShipmentId("S-001");
        }
    }
}
