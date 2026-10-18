package com.example.shop.order.domain;

public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID は空にできません");
        }
    }
}
