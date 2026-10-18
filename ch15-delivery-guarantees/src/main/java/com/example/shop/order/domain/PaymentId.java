package com.example.shop.order.domain;

public record PaymentId(String value) {

    public PaymentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID は空にできません");
        }
    }
}
