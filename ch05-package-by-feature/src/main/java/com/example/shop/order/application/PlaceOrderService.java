package com.example.shop.order.application;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderService {

    private final OrderRepository orders;

    PlaceOrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public UUID place(String customerId) {
        var order = new Order(UUID.randomUUID(), customerId);
        orders.save(order);
        return order.id();
    }
}
