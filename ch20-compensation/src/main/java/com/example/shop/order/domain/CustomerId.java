package com.example.shop.order.domain;

public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID は空にできません");
        }
    }
}
