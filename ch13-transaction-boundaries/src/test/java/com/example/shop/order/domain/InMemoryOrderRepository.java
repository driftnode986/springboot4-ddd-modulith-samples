package com.example.shop.order.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** テスト用の実装。保存先が入れ替わってもドメインのテストは変わらないことを示す。 */
class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> store = new HashMap<>();

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }
}
