package com.example.shop.order.domain;

import java.util.UUID;

public class Order {

    private final UUID id;
    private final String customerId;

    public Order(UUID id, String customerId) {
        this.id = id;
        this.customerId = customerId;
    }

    public UUID id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }
}
