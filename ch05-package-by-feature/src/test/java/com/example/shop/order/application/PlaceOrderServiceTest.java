package com.example.shop.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceOrderServiceTest {

    @Test
    @DisplayName("注文を保存し、在庫と支払いを公開面を通して頼む")
    void 注文を保存し在庫と支払いを頼む() {
        Map<UUID, Order> saved = new HashMap<>();
        OrderRepository orders = new OrderRepository() {
            @Override
            public void save(Order order) {
                saved.put(order.id(), order);
            }

            @Override
            public Optional<Order> findById(UUID id) {
                return Optional.ofNullable(saved.get(id));
            }
        };
        List<String> reserved = new ArrayList<>();
        List<UUID> paid = new ArrayList<>();
        var service = new PlaceOrderService(orders,
                (sku, quantity) ->
                        reserved.add(sku + " x " + quantity),
                paid::add);

        UUID id = service.place("C-1", "SKU-1", 2);

        assertThat(saved.get(id).customerId()).isEqualTo("C-1");
        assertThat(reserved).containsExactly("SKU-1 x 2");
        assertThat(paid).containsExactly(id);
    }
}
