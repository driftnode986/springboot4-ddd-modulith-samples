package com.example.shop.order.domain;

// #region order-repository
import java.util.Optional;

public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);
}
// #endregion
