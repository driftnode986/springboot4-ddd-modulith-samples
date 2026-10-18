package com.example.shop.order.domain;

public record ReservationId(String value) {

    public ReservationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID は空にできません");
        }
    }
}
