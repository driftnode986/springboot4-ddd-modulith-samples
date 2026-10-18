package com.example.shop.order.domain;

public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID は空にできません");
        }
    }
}
